package com.joa.prexixion.jobs.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.JobStatus;
import com.joa.prexixion.jobs.repository.JobStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobStatusService {
    private final JobStatusRepository repo;
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

    public JobStatus obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("JobStatus no encontrado"));
    }
}