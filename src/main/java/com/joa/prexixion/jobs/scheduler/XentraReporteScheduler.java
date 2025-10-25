package com.joa.prexixion.jobs.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.XentraReporte;
import com.joa.prexixion.jobs.repository.XentraReporteRepository;

@Service
public class XentraReporteScheduler {

    @Autowired
    private XentraReporteRepository xentraReporteRepository;

    // @Scheduled(cron = "0 0 17 * * *") // Todos a hora especifica
    @Scheduled(cron = "0 0 4 * * *") // Todos los días a medianoche
    public void bloquearReportesVencidos() {
        System.out.println("Entrando a ejecutar job Xentra Reporte");
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.minusDays(2); // Antes de ayer
        List<XentraReporte> reportes = xentraReporteRepository.obtenerXentraReporteParaBloqueo(fechaLimite);

        for (XentraReporte reporte : reportes) {
            System.out.println("Cambiando estadoLogico a BLOQUEADO");
            reporte.setEstadoLogico("BLOQUEADO");
        }

        xentraReporteRepository.saveAll(reportes);
    }
}
