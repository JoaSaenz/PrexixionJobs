package com.joa.prexixion.jobs.scheduler;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.joa.prexixion.jobs.service.DeudaService;

@Component
public class DeudaScheduler {

    @Autowired
    private DeudaService deudaService;

    //@Scheduled(cron = "0 0 0 * * ?") // Ejecuta la tarea a medianoche
    @Scheduled(cron = "0 59 12 * * ?") // Ejecuta la tarea a medianoche
    public void actualizarDeudasVencidas() {
        System.out.println("⏳ Verificando deudas vencidas... " + LocalDateTime.now());
        deudaService.actualizarDeudasCoactivas();
    }
}
