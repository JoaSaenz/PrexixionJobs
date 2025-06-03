package com.joa.prexixion.jobs.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.XentraReporte;
import com.joa.prexixion.jobs.repository.XentraReporteRepository;

@Service
public class XentraReporteScheduler {

    @Autowired
    private XentraReporteRepository xentraReporteRepository;

    @Scheduled(cron = "0 0 0 * * *") // Todos los días a medianoche
    public void bloquearReportesVencidos() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.minusDays(1); // ayer
        List<XentraReporte> reportes = xentraReporteRepository.obtenerXentraReporteParaBloqueo(fechaLimite);

        for (XentraReporte reporte : reportes) {
            reporte.setEstadoLogico("BLOQUEADO");
        }

        xentraReporteRepository.saveAll(reportes);
    }
}
