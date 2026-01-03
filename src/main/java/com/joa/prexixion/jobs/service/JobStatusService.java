package com.joa.prexixion.jobs.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.JobStatus;
import com.joa.prexixion.jobs.model.JobStatusLog;
import com.joa.prexixion.jobs.model.Notificacion;
import com.joa.prexixion.jobs.repository.JobStatusLogRepository;
import com.joa.prexixion.jobs.repository.JobStatusRepository;
import com.joa.prexixion.jobs.repository.NotificacionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobStatusService {
    private final JobStatusRepository repo;
    private final JobStatusLogRepository logRepo;
    private final NotificacionRepository notificacionRepo;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Crear un nuevo registro JobStatus por ejecución */
    public JobStatus iniciarEjecucion(String nombreJob) {

        JobStatus job = JobStatus.builder()
                .nombreJob(nombreJob)
                .estado("EN_PROGRESO")
                .fechaEjecucion(LocalDate.now())
                .horaInicio(LocalDateTime.now())
                .progreso(0.0)
                .rucsOk(0)
                .rucsNoOk(0)
                .mensaje("Iniciando ejecución...")
                .ultimaActualizacion(LocalDateTime.now().format(fmt))
                .build();

        return repo.save(job);
    }

    /** Actualizar progreso, mensaje o estado mientras avanza el job */
    public JobStatus actualizar(JobStatus job, String estado, double progreso, String mensaje) {
        job.setEstado(estado);
        job.setProgreso(progreso);
        job.setMensaje(mensaje);
        job.setUltimaActualizacion(LocalDateTime.now().format(fmt));
        return repo.save(job);
    }

    /** Marcar finalización de ejecución */
    public JobStatus finalizarEjecucion(JobStatus job, String estadoFinal, double progresoFinal,
            String mensajeFinal) {
        job.setEstado(estadoFinal);
        job.setProgreso(progresoFinal);
        job.setMensaje(mensajeFinal);
        job.setHoraFin(LocalDateTime.now());
        job.setUltimaActualizacion(LocalDateTime.now().format(fmt));
        return repo.save(job);
    }

    public JobStatus obtener(String nombreJob) {
        return repo.findTopByNombreJobOrderByHoraInicioDesc(nombreJob)
                .orElse(JobStatus.builder()
                        .nombreJob(nombreJob)
                        .estado("SIN_INICIAR")
                        .progreso(0.0)
                        .mensaje("Esperando ejecución...")
                        .build());
    }

    /**
     * Listar todas las ejecuciones de un job específico, ordenadas por más reciente
     * primero
     */
    public List<JobStatus> listarPorNombreJob(String nombreJob) {
        return repo.findTop7ByNombreJobOrderByHoraInicioDesc(nombreJob);
    }

    public JobStatus obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("JobStatus no encontrado"));
    }

    public List<JobStatusLog> obtenerLogs(Long jobStatusId) {
        JobStatus jobStatus = obtenerPorId(jobStatusId);
        return logRepo.findByJobStatusOrderByFechaRegistroAsc(jobStatus);
    }

    public List<Notificacion> obtenerNotificaciones(Long jobStatusId) {
        return notificacionRepo.findByJobStatusIdOrderByFechaDesc(jobStatusId);
    }
}