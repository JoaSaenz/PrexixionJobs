package com.joa.prexixion.jobs.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.joa.prexixion.jobs.dto.ClienteDTO;
import com.joa.prexixion.jobs.dto.NotificacionDTO;
import com.joa.prexixion.jobs.dto.SunatBuzonResponseDTO;
import com.joa.prexixion.jobs.model.Cliente;
import com.joa.prexixion.jobs.model.JobStatus;
import com.joa.prexixion.jobs.model.JobStatusLog;
import com.joa.prexixion.jobs.model.Notificacion;
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
    public CompletableFuture<Long> ejecutarAsync() {
        return sincronizarBuzones();
    }

    public CompletableFuture<Long> sincronizarBuzones() {

        JobStatus job = jobStatusService.iniciarEjecucion("SincronizacionSUNAT");
        Long jobId = job.getId();

        List<Cliente> clientes = clienteRepository.obtenerClientesTest10();
        int total = clientes.size();

        int procesados = 0;
        int ok = 0;
        int noOk = 0;

        for (Cliente cliente : clientes) {

            long inicioProcesoMs = System.currentTimeMillis();
            String resultado = "ERROR";
            String mensaje = "Sin procesar";
            int nuevas = 0;

            try {

                ClienteDTO dto = new ClienteDTO(cliente.getRuc(), cliente.getSolU(), cliente.getSolC());
                SunatBuzonResponseDTO response = llamarServicioNode(dto);

                // Node no responde
                if (response == null) {
                    return finalizarJobPorError(job, procesados, total, cliente,
                            "ERROR_INTERNO", "Node no devolvió respuesta", inicioProcesoMs);
                }

                // Node responde error
                if (!Boolean.TRUE.equals(response.isSuccess())) {

                    String tipo = response.getType();

                    switch (tipo) {
                        case "ERROR_SUNAT":
                            return finalizarJobPorError(job, procesados, total, cliente,
                                    "ERROR_CRITICO", response.getMessage(), inicioProcesoMs);

                        case "CREDENCIALES_INVALIDAS":
                            noOk++;
                            resultado = "CREDENCIALES_INVALIDAS";
                            mensaje = "Credenciales erróneas";
                            guardarLogRuc(job, cliente, resultado, mensaje, inicioProcesoMs, 0);
                            continue;

                        case "ERROR_INTERNO":
                        case "ERROR_HTTP":
                        case "TIMEOUT_NODE":
                        case "NODE_CAIDO":
                            return finalizarJobPorError(job, procesados, total, cliente,
                                    tipo, response.getMessage(), inicioProcesoMs);
                    }
                }

                // Consultado OK
                ok++;
                resultado = "OK";
                mensaje = "Consulta exitosa";

                if (response.getNotificaciones() != null) {
                    nuevas = procesarNotificaciones(dto, response.getNotificaciones());
                }

            } catch (Exception e) {
                return finalizarJobPorError(job, procesados, total, cliente,
                        "ERROR", e.getMessage(), inicioProcesoMs);
            }

            guardarLogRuc(job, cliente, resultado, mensaje, inicioProcesoMs, nuevas);
            procesados++;

            job.setRucsOk(ok);
            job.setRucsNoOk(noOk);

            double progreso = (procesados * 100.0) / total;
            jobStatusService.actualizar(job, "EN_PROGRESO", progreso,
                    "Procesado RUC " + cliente.getRuc());
        }

        jobStatusService.finalizarEjecucion(job, "FINALIZADO");
        return CompletableFuture.completedFuture(jobId);
    }

    // ======================================================
    // MÉTODOS PRIVADOS
    // ======================================================

    private SunatBuzonResponseDTO llamarServicioNode(ClienteDTO dto) {
        String url = "http://localhost:3000/sunat/consultar";
        return restTemplate.postForObject(url, dto, SunatBuzonResponseDTO.class);
    }

    private CompletableFuture<Long> finalizarJobPorError(
            JobStatus job, int procesados, int total, Cliente cliente,
            String tipoError, String mensaje, long inicioMs) {

        double progresoActual = (procesados * 100.0) / total;

        jobStatusService.actualizar(job, "ERROR", progresoActual,
                mensaje);

        jobStatusService.finalizarEjecucion(job, "ERROR");

        guardarLogRuc(job, cliente, tipoError, mensaje, inicioMs, 0);

        return CompletableFuture.completedFuture(job.getId());
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

    private int procesarNotificaciones(ClienteDTO clienteDTO, List<NotificacionDTO> notificacionesDTO) {
        if (notificacionesDTO == null || notificacionesDTO.isEmpty()) {
            return 0;
        }

        List<String> ids = notificacionesDTO.stream().map(NotificacionDTO::getId).toList();
        List<String> existentes = notificacionRepository.findExistentes(clienteDTO.getRuc(), ids);

        List<Notificacion> nuevas = notificacionesDTO.stream()
                .filter(n -> !existentes.contains(n.getId()))
                .map(n -> {
                    Notificacion entidad = new Notificacion();
                    entidad.setRuc(clienteDTO.getRuc());
                    entidad.setIdSunat(n.getId());
                    entidad.setTitulo(n.getTitulo());
                    entidad.setFecha(LocalDateTime.parse(
                            n.getFecha(),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    return entidad;
                })
                .toList();

        if (!nuevas.isEmpty()) {
            notificacionRepository.saveAll(nuevas);
        }

        return nuevas.size();
    }
}