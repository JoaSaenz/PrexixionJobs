package com.joa.prexixion.jobs.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.Deuda;
import com.joa.prexixion.jobs.repository.DeudaRepository;
import com.joa.prexixion.jobs.service.DeudaService;

@Service
public class DeudaScheduler {

    @Autowired
    private DeudaService deudaService;

    @Autowired
    private DeudaRepository deudaRepository;

    // @Scheduled(cron = "0 0 0 * * ?") // Ejecuta la tarea a medianoche
    //@Scheduled(cron = "0 58 16 * * ?") // Ejecuta la tarea a medianoche
    //public void actualizarDeudasVencidas() {
    //    System.out.println("⏳ Verificando deudas vencidas... " +
    //            LocalDateTime.now());
    //    deudaService.actualizarDeudasCoactivas();
    //}

    // @Scheduled(cron = "0 39 16 * * ?") // Prueba para obtener la lista de deudas
    // de un cliente
    // public void listarDeudas() {
    // List<Deuda> deudas = deudaRepository.obtenerDeudas("20603018142");
    // System.out.println("Deudas encontradas: " + deudas.size());
    // }
}
