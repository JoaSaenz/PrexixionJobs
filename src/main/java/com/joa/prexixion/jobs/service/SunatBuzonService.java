package com.joa.prexixion.jobs.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.joa.prexixion.jobs.dto.ClienteDTO;
import com.joa.prexixion.jobs.dto.NotificacionDTO;
import com.joa.prexixion.jobs.dto.SunatBuzonResponseDTO;
import com.joa.prexixion.jobs.model.Cliente;
import com.joa.prexixion.jobs.model.TipologiaNotificacion;
import com.joa.prexixion.jobs.model.JobStatus;
import com.joa.prexixion.jobs.model.JobStatusLog;
import com.joa.prexixion.jobs.model.Notificacion;
import com.joa.prexixion.jobs.model.NotificacionAdjunto;
import com.joa.prexixion.jobs.repository.ClienteRepository;
import com.joa.prexixion.jobs.repository.JobStatusLogRepository;
import com.joa.prexixion.jobs.repository.NotificacionRepository;

@Service
public class SunatBuzonService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private NotificacionRepository notificacionRepository;
    @Autowired
    private JobStatusService jobStatusService;
    @Autowired
    private JobStatusLogRepository logRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public SunatBuzonService() {
        this.objectMapper = new ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false);
        try {
            this.objectMapper.getFactory().setStreamReadConstraints(
                    com.fasterxml.jackson.core.StreamReadConstraints.builder()
                            .maxStringLength(Integer.MAX_VALUE)
                            .build());
        } catch (Throwable t) {
            // Ignore if older Jackson version
        }
    }

    @Async
    public CompletableFuture<Long> sincronizarBuzones() {

        JobStatus job = jobStatusService.iniciarEjecucion("SincronizacionSUNAT");
        Long jobId = job.getId();

        AtomicInteger procesados = new AtomicInteger(0);
        AtomicInteger ok = new AtomicInteger(0);
        AtomicInteger noOk = new AtomicInteger(0);
        AtomicBoolean errorCritico = new AtomicBoolean(false);
        Object lock = new Object(); // Para sincronizar actualizaciones de DB

        List<Cliente> clientes = clienteRepository.obtenerClientes();
        int total = clientes.size();

        // Pool de 4 hilos para procesar en paralelo
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            // Creamos una lista de futuros
            List<CompletableFuture<Void>> futures = clientes.stream()
                    .map(cliente -> CompletableFuture.runAsync(() -> {
                        // Si ya hubo error crítico, abortamos (similar al break)
                        if (errorCritico.get())
                            return;

                        long inicioMs = System.currentTimeMillis();
                        String resultado = "ERROR";
                        String mensaje = "Sin procesar";
                        int nuevas = 0;

                        try {
                            // 1. Validación previa en Java para evitar llamadas innecesarias a Node
                            if (cliente.getSolU() == null || cliente.getSolU().isBlank() ||
                                    cliente.getSolC() == null || cliente.getSolC().isBlank()) {
                                resultado = "ERROR_SIN_CREDENCIALES";
                                mensaje = "Credenciales SOL incompletas en base de datos";
                                noOk.incrementAndGet();
                                guardarLogRuc(job, cliente, resultado, mensaje, inicioMs, nuevas);
                                procesados.incrementAndGet();
                                return;
                            }

                            ClienteDTO dto = new ClienteDTO(
                                    cliente.getRuc(),
                                    cliente.getSolU(),
                                    cliente.getSolC());

                            SunatBuzonResponseDTO response;
                            try {
                                response = llamarServicioNode(dto);
                            } catch (HttpClientErrorException e) {
                                throw e; // Relanzar para el catch específico
                            } catch (Exception e) {
                                // Error de JSON o de comunicación: lo tratamos como un error de estE RUC
                                // solamente
                                resultado = "ERROR_COMUNICACION";
                                mensaje = "Error comunicación/JSON: "
                                        + (e.getMessage() != null ? e.getMessage() : e.toString());
                                noOk.incrementAndGet();
                                guardarLogRuc(job, cliente, resultado, mensaje, inicioMs, nuevas);
                                procesados.incrementAndGet();
                                return; // Salir de este RUC pero NO marcar errorCritico
                            }

                            if (response == null) {
                                throw new IllegalStateException("Node no devolvió respuesta");
                            }

                            if (!Boolean.TRUE.equals(response.isSuccess())) {

                                switch (response.getType() != null ? response.getType() : "ERROR_DESCONOCIDO") {

                                    case "CREDENCIALES_INVALIDAS":
                                        resultado = "CREDENCIALES_INVALIDAS";
                                        mensaje = "Credenciales SOL no válidas en SUNAT";
                                        noOk.incrementAndGet();
                                        break;

                                    case "ERROR_SUNAT":
                                    case "ERROR_INTERNO":
                                    case "ERROR_HTTP":
                                    case "TIMEOUT_NODE":
                                    case "NODE_CAIDO":
                                        resultado = response.getType();
                                        mensaje = response.getMessage() != null ? response.getMessage()
                                                : response.getError();
                                        noOk.incrementAndGet();
                                        break;

                                    default:
                                        resultado = "ERROR_DESCONOCIDO";
                                        mensaje = response.getError() != null ? response.getError()
                                                : (response.getMessage() != null ? response.getMessage()
                                                        : "Detalle desconocido");
                                        noOk.incrementAndGet();
                                        break;
                                }

                            } else {
                                // OK
                                resultado = "OK";
                                mensaje = "Consulta exitosa";
                                ok.incrementAndGet();

                                if (response.getNotificaciones() != null) {
                                    nuevas = procesarNotificaciones(dto, response.getNotificaciones(), jobId);
                                }
                            }

                        } catch (HttpClientErrorException e) {
                            resultado = "ERROR_HTTP";
                            if (e.getStatusCode().value() == 400) {
                                resultado = "ERROR_SIN_CREDENCIALES";
                                mensaje = "Node reportó credenciales faltantes (400)";
                            } else {
                                mensaje = "Error HTTP " + e.getStatusCode();
                            }
                            noOk.incrementAndGet();

                        } catch (Exception e) {
                            resultado = "ERROR_CRITICO";
                            mensaje = normalizarMensaje(e);
                            // Solo errores realmente inesperados (como fallos de BD) detienen el batch
                            // errorCritico.set(true); // Comentado temporalmente para dejar que el proceso
                            // avance
                            noOk.incrementAndGet();
                        }

                        // Guardamos el log INDIVIDUAL
                        guardarLogRuc(job, cliente, resultado, mensaje, inicioMs, nuevas);

                        int currentProcesados = procesados.incrementAndGet();

                        // Sincronizamos la actualización del Job principal para evitar conflictos
                        synchronized (lock) {
                            job.setRucsOk(ok.get());
                            job.setRucsNoOk(noOk.get());

                            double progreso = (currentProcesados * 100.0) / total;

                            // Solo actualizamos mensaje de progreso de vez en cuando o siempre?
                            // Siempre está bien, es informativo.
                            jobStatusService.actualizar(
                                    job,
                                    "EN_PROGRESO",
                                    progreso,
                                    "Procesando... " + currentProcesados + "/" + total);
                        }

                    }, executor))
                    .collect(Collectors.toList());

            // Esperamos a que TODOS terminen
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            // Importante: apagar el executor para liberar hilos
            executor.shutdown();

            String estadoFinal = errorCritico.get() ? "ERROR" : "FINALIZADO";

            String mensajeFinal = errorCritico.get()
                    ? "Ejecución detenida (parcialmente) por error crítico"
                    : String.format(
                            "Ejecución completada: %d OK, %d con error",
                            ok.get(),
                            noOk.get());

            double progresoFinal = errorCritico.get()
                    ? job.getProgreso()
                    : 100.0;

            jobStatusService.finalizarEjecucion(job, estadoFinal, progresoFinal, mensajeFinal);
        }

        return CompletableFuture.completedFuture(jobId);
    }
    // ======================================================
    // MÉTODOS PRIVADOS
    // ======================================================

    private SunatBuzonResponseDTO llamarServicioNode(ClienteDTO dto) {
        String url = "http://localhost:3000/sunat/consultar";

        // Obtenemos la respuesta como String primero para diagnóstico si falla el mapeo
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, dto, String.class);
        String rawJson = responseEntity.getBody();

        try {
            return objectMapper.readValue(rawJson, SunatBuzonResponseDTO.class);
        } catch (Exception e) {
            // Si falla la extracción, lanzamos con el contenido crudo para ver qué envió
            // Node realmente
            throw new RuntimeException("Error mapeo JSON. Contenido recibido: " + rawJson, e);
        }
    }

    private void guardarLogRuc(JobStatus job, Cliente cliente,
            String resultado, String mensaje, long inicioMs, int nuevasNotificaciones) {

        JobStatusLog log = JobStatusLog.builder()
                .ruc(cliente.getRuc())
                .y(cliente.getY())
                .resultado(limitar(resultado, 50))
                .mensaje(limitar(mensaje, 250)) // Forzamos 250 por si la BD no se actualizó
                .duracionMs(System.currentTimeMillis() - inicioMs)
                .nuevasNotificaciones(nuevasNotificaciones)
                .fechaRegistro(LocalDateTime.now())
                .jobStatus(job)
                .build();

        logRepository.save(log);
    }

    private int procesarNotificaciones(ClienteDTO clienteDTO, List<NotificacionDTO> notificacionesDTO,
            Long jobStatusId) {
        if (notificacionesDTO == null || notificacionesDTO.isEmpty()) {
            return 0;
        }

        List<String> ids = notificacionesDTO.stream().map(NotificacionDTO::getId).toList();
        // Obtener entidades completas para verificar adjuntos
        List<Notificacion> existentes = notificacionRepository.findByRucAndIdSunatIn(clienteDTO.getRuc(), ids);

        int procesadasCount = 0;

        for (NotificacionDTO nDTO : notificacionesDTO) {
            Notificacion entidad = existentes.stream()
                    .filter(e -> e.getIdSunat().equals(nDTO.getId()))
                    .findFirst()
                    .orElse(null);

            boolean esNueva = (entidad == null);

            if (esNueva) {
                entidad = new Notificacion();
                entidad.setRuc(clienteDTO.getRuc());
                entidad.setIdSunat(nDTO.getId());
                entidad.setTitulo(nDTO.getTitulo());
                entidad.setFecha(parseFechaSunat(nDTO.getFecha()));
                entidad.setJobStatusId(jobStatusId);

                TipologiaNotificacion tipologia = TipologiaNotificacion.clasificar(nDTO.getTitulo());
                entidad.setTipo(tipologia.getTipo());
                entidad.setNombreCorto(tipologia.getNombreCorto());
            }

            // Solo agregamos adjuntos si:
            // 1. Es nueva OR
            // 2. Ya existe pero no tiene adjuntos (para recuperar archivos de
            // notificaciones viejas)
            if (nDTO.getAdjuntos() != null && !nDTO.getAdjuntos().isEmpty()) {
                if (esNueva || (entidad.getAdjuntos() == null || entidad.getAdjuntos().isEmpty())) {
                    for (NotificacionDTO.AdjuntoDTO adjDTO : nDTO.getAdjuntos()) {
                        NotificacionAdjunto adj = new NotificacionAdjunto();
                        adj.setNombre(adjDTO.getNombre());
                        try {
                            adj.setContenido(Base64.getDecoder().decode(adjDTO.getBase64()));
                        } catch (Exception e) {
                            System.err.println(
                                    "⚠️ Error decodificando adjunto " + adjDTO.getNombre() + ": " + e.getMessage());
                        }
                        adj.setNotificacion(entidad);
                        entidad.getAdjuntos().add(adj);
                    }

                    if (esNueva) {
                        procesadasCount++;
                    } else {
                        // Si no era nueva pero le agregamos adjuntos, debemos guardarla también
                        // El repository.saveAll la actualizará
                    }
                }
            } else if (esNueva) {
                procesadasCount++;
            }

            // Si es nueva o si fue actualizada (porque le agregamos adjuntos), la guardamos
            notificacionRepository.save(entidad);
        }

        return procesadasCount;
    }

    private LocalDateTime parseFechaSunat(String fecha) {
        if (fecha == null || fecha.isBlank())
            return null;

        try {
            return LocalDateTime.parse(
                    fecha.replace(";", " "),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        } catch (Exception e) {
            // log técnico, NO romper el job
            System.err.println("⚠️ Fecha SUNAT inválida: " + fecha);
            return null;
        }
    }

    private String normalizarMensaje(Exception e) {
        if (e == null)
            return "Error desconocido";
        String msg = e.getMessage() != null ? e.getMessage() : e.toString();
        return limitar(msg.replace(";", "").replace("\n", " ").trim(), 250);
    }

    private String limitar(String s, int len) {
        if (s == null)
            return null;
        if (s.length() <= len)
            return s;
        return s.substring(0, len - 3) + "...";
    }
}