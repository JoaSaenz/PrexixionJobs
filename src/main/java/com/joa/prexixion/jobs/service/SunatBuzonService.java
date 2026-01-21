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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.joa.prexixion.jobs.dto.ClienteDTO;
import com.joa.prexixion.jobs.dto.NotificacionDTO;
import com.joa.prexixion.jobs.dto.SunatBuzonResponseDTO;
import com.joa.prexixion.jobs.model.Cliente;
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

    @Async
    public CompletableFuture<Long> sincronizarBuzones() {

        JobStatus job = jobStatusService.iniciarEjecucion("SincronizacionSUNAT");
        Long jobId = job.getId();

        AtomicInteger procesados = new AtomicInteger(0);
        AtomicInteger ok = new AtomicInteger(0);
        AtomicInteger noOk = new AtomicInteger(0);
        AtomicBoolean errorCritico = new AtomicBoolean(false);
        Object lock = new Object(); // Para sincronizar actualizaciones de DB

        List<Cliente> clientes = clienteRepository.obtenerClientesTest10();
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
                            ClienteDTO dto = new ClienteDTO(
                                    cliente.getRuc(),
                                    cliente.getSolU(),
                                    cliente.getSolC());

                            SunatBuzonResponseDTO response = llamarServicioNode(dto);

                            if (response == null) {
                                throw new IllegalStateException("Node no devolvió respuesta");
                            }

                            if (!Boolean.TRUE.equals(response.isSuccess())) {

                                switch (response.getType()) {

                                    case "CREDENCIALES_INVALIDAS":
                                        resultado = "CREDENCIALES_INVALIDAS";
                                        mensaje = "Credenciales erróneas";
                                        noOk.incrementAndGet();
                                        break;

                                    case "ERROR_SUNAT":
                                    case "ERROR_INTERNO":
                                    case "ERROR_HTTP":
                                    case "TIMEOUT_NODE":
                                    case "NODE_CAIDO":
                                        // Error operativo, NO crítico.
                                        resultado = response.getType();
                                        mensaje = response.getMessage();
                                        noOk.incrementAndGet();
                                        break;

                                    default:
                                        throw new RuntimeException("Error desconocido: " + response.getType());
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
                            if (e.getStatusCode().value() == 400) {
                                resultado = "ERROR_SIN_CREDENCIALES";
                                mensaje = "Credenciales incompletas o vacías";
                                noOk.incrementAndGet();
                            } else {
                                resultado = "ERROR_HTTP";
                                mensaje = "Error HTTP " + e.getStatusCode();
                                noOk.incrementAndGet();
                            }

                        } catch (Exception e) {
                            resultado = "ERROR_CRITICO";
                            mensaje = normalizarMensaje(e);
                            errorCritico.set(true);
                            noOk.incrementAndGet();
                        }

                        // Guardamos el log INDIVIDUAL (thread-safe si repository lo es)
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
        return restTemplate.postForObject(url, dto, SunatBuzonResponseDTO.class);
    }

    private void guardarLogRuc(JobStatus job, Cliente cliente,
            String resultado, String mensaje, long inicioMs, int nuevasNotificaciones) {

        JobStatusLog log = JobStatusLog.builder()
                .ruc(cliente.getRuc())
                .y(cliente.getY())
                .resultado(resultado)
                .mensaje(mensaje)
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
        if (e == null || e.getMessage() == null) {
            return "Error inesperado";
        }
        return e.getMessage()
                .replace(";", "")
                .replace("\n", " ")
                .trim();
    }
}