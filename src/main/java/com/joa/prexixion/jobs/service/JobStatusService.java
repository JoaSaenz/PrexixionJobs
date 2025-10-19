package com.joa.prexixion.jobs.service;

import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.JobStatus;
import com.joa.prexixion.jobs.repository.JobStatusRepository;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class JobStatusService {
    private final JobStatusRepository repo;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void actualizar(String nombreJob, String estado, double progreso, String mensaje) {
        JobStatus status = repo.findByNombreJob(nombreJob)
                .orElse(JobStatus.builder()
                        .nombreJob(nombreJob)
                        .build());
        status.setEstado(estado);
        status.setProgreso(progreso);
        status.setMensaje(mensaje);
        status.setUltimaActualizacion(LocalDateTime.now().format(fmt));
        repo.save(status);
    }

    public JobStatus obtener(String nombreJob) {
        return repo.findByNombreJob(nombreJob)
                .orElse(JobStatus.builder()
                        .nombreJob(nombreJob)
                        .estado("SIN_INICIAR")
                        .progreso(0.0)
                        .mensaje("Esperando ejecución...")
                        .build());
    }
}