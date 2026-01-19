package com.joa.prexixion.jobs.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.joa.prexixion.jobs.service.GestionArrendamientoContratoService;

@Component
public class GestionArrendamientoContratoScheduler {

    private final GestionArrendamientoContratoService service;

    public GestionArrendamientoContratoScheduler(GestionArrendamientoContratoService service) {
        this.service = service;
    }

    /**
     * Tarea programada que se ejecuta todos los días a las 00:00 horas
     * Actualiza los estados de los contratos de arrendamiento según sus fechas de
     * vencimiento
     */
    @Scheduled(cron = "0 00 00 * * *") // Todos los días a las 00:00
    public void actualizarEstadosContratos() {
        System.out.println(">>> Ejecutando tarea programada: Actualización de estados de contratos");
        service.actualizarEstadosContratos();
        System.out.println(">>> Tarea programada finalizada: Actualización de estados de contratos");
    }
}
