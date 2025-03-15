package com.joa.prexixion.jobs.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.Deuda;
import com.joa.prexixion.jobs.repository.DeudaRepository;

@Service
public class DeudaService {

    @Autowired
    private DeudaRepository deudaRepository;

    public void actualizarDeudasCoactivas() {
        LocalDate fechaInicio = LocalDate.of(2000, 1, 1); // '2000-01-01'
        LocalDate fechaActual = LocalDate.now(); // Fecha actual

        System.out.println(fechaInicio);
        System.out.println(fechaActual);

        int filasActualizadas = deudaRepository.marcarDeudasComoCoactivas("20603018142", fechaInicio, fechaActual);
        System.out.println("✅ Se actualizaron " + filasActualizadas + " deudas a estado COACTIVA.");

        // List<Deuda> lista = deudaRepository.obtenerDeudas("20603018142");
        // System.out.println("Tamaño de la lista: " + lista.size());
        // for (Deuda deuda : lista) {
        // System.out.println(deuda.toString());
        // }

    }
}
