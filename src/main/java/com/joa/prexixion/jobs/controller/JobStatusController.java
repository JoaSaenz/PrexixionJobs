package com.joa.prexixion.jobs.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.joa.prexixion.jobs.model.JobStatus;
import com.joa.prexixion.jobs.model.JobStatusLog;
import com.joa.prexixion.jobs.model.Notificacion;
import com.joa.prexixion.jobs.service.JobStatusService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/job-status")
@RequiredArgsConstructor
public class JobStatusController {

    private final JobStatusService service;

    @GetMapping("/{nombreJob}")
    public JobStatus obtenerEstado(@PathVariable String nombreJob) {
        return service.obtener(nombreJob);
    }

    @GetMapping("/detail/{id}")
    public JobStatus obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/list/{nombreJob}")
    public List<JobStatus> listarEjecuciones(@PathVariable String nombreJob) {
        return service.listarPorNombreJob(nombreJob);
    }

    @GetMapping("/{jobStatusId}/logs")
    public List<JobStatusLog> obtenerLogs(@PathVariable Long jobStatusId) {
        return service.obtenerLogs(jobStatusId);
    }

    @GetMapping("/{jobStatusId}/notificaciones")
    public List<Notificacion> obtenerNotificaciones(@PathVariable Long jobStatusId) {
        return service.obtenerNotificaciones(jobStatusId);
    }
}