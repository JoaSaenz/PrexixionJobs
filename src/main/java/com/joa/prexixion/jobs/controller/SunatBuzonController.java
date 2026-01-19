package com.joa.prexixion.jobs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.joa.prexixion.jobs.dto.ApiResponse;
import com.joa.prexixion.jobs.service.SunatBuzonService;

import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.EnableAsync;

@RestController
@RequestMapping("/api/sunat")
@EnableAsync
public class SunatBuzonController {

    private final SunatBuzonService sunatBuzonService;

    public SunatBuzonController(SunatBuzonService sunatBuzonService) {
        this.sunatBuzonService = sunatBuzonService;
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<ApiResponse> sincronizarManualmente() {
        // Lanza la sincronización en segundo plano usando @Async
        sunatBuzonService.sincronizarBuzones(); // 👉 SE LANZA EN SEGUNDO PLANO Y NO SE ESPERA

        // Devuelve una respuesta JSON estándar
        ApiResponse response = new ApiResponse(
                "OK",
                "Sincronización manual iniciada correctamente. Puede tardar varios minutos.",
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}