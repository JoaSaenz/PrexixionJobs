package com.joa.prexixion.jobs.controller;

import org.springframework.web.bind.annotation.*;

import com.joa.prexixion.jobs.model.JobStatus;
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
}