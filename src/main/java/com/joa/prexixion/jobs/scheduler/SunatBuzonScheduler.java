package com.joa.prexixion.jobs.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.joa.prexixion.jobs.service.SunatBuzonService;

@Component
public class SunatBuzonScheduler {

    private final SunatBuzonService sunatBuzonService;

    public SunatBuzonScheduler(SunatBuzonService sunatBuzonService) {
        this.sunatBuzonService = sunatBuzonService;
    }

    @Scheduled(cron = "0 52 10 * * *") // todos los días a las 00:00
    public void ejecutarJob() {
        sunatBuzonService.sincronizarBuzones();
    }
}