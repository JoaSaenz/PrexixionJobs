package com.joa.prexixion.jobs.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.joa.prexixion.jobs.model.GestionArrendamientoContrato;
import com.joa.prexixion.jobs.repository.GestionArrendamientoContratoRepository;

@Service
public class GestionArrendamientoContratoService {

    private final GestionArrendamientoContratoRepository repository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Estados
    private static final String ESTADO_VIGENTE = "VIGENTE";
    private static final String ESTADO_POR_VENCER = "POR VENCER";
    private static final String ESTADO_VENCIDO = "VENCIDO";

    // Días de anticipación para marcar como "POR VENCER"
    private static final int DIAS_ANTICIPACION = 15;

    public GestionArrendamientoContratoService(GestionArrendamientoContratoRepository repository) {
        this.repository = repository;
    }

    /**
     * Actualiza los estados de los contratos según las fechas de vencimiento
     */
    public void actualizarEstadosContratos() {
        System.out.println("=== Iniciando actualización de estados de contratos ===");

        // Obtener fecha actual
        LocalDate fechaActual = LocalDate.now();
        System.out.println("Fecha actual: " + fechaActual.format(DATE_FORMATTER));

        // Obtener todos los contratos en estado VIGENTE o POR VENCER
        List<String> estadosAConsultar = Arrays.asList(ESTADO_VIGENTE, ESTADO_POR_VENCER);
        List<GestionArrendamientoContrato> contratos = repository.findByEstadoLogicoIn(estadosAConsultar);

        System.out.println("Contratos encontrados para revisar: " + contratos.size());

        // Lista para almacenar los contratos que necesitan actualización
        List<GestionArrendamientoContrato> contratosParaActualizar = new ArrayList<>();

        int contratosVencidos = 0;
        int contratosPorVencer = 0;

        for (GestionArrendamientoContrato contrato : contratos) {
            try {
                // Parsear la fecha de fin
                LocalDate fechaFin = LocalDate.parse(contrato.getFechaFin(), DATE_FORMATTER);
                String estadoActual = contrato.getEstadoLogico();
                String nuevoEstado = null;

                // Determinar el nuevo estado según la lógica de negocio
                if (fechaFin.isBefore(fechaActual)) {
                    // La fecha de fin ya pasó -> VENCIDO
                    if (!ESTADO_VENCIDO.equals(estadoActual)) {
                        nuevoEstado = ESTADO_VENCIDO;
                        contratosVencidos++;
                    }
                } else if (fechaFin.minusDays(DIAS_ANTICIPACION).isBefore(fechaActual) ||
                        fechaFin.minusDays(DIAS_ANTICIPACION).isEqual(fechaActual)) {
                    // La fecha de fin está a 15 días o menos -> POR VENCER
                    // Solo cambiar si actualmente está VIGENTE
                    if (ESTADO_VIGENTE.equals(estadoActual)) {
                        nuevoEstado = ESTADO_POR_VENCER;
                        contratosPorVencer++;
                    }
                }

                // Si hay un cambio de estado, actualizar el contrato
                if (nuevoEstado != null) {
                    System.out.println(String.format(
                            "Contrato ID %d: %s -> %s (Fecha fin: %s)",
                            contrato.getId(),
                            estadoActual,
                            nuevoEstado,
                            contrato.getFechaFin()));

                    contrato.setEstadoLogico(nuevoEstado);
                    contratosParaActualizar.add(contrato);
                }

            } catch (Exception e) {
                System.err.println(String.format(
                        "Error al procesar contrato ID %d: %s",
                        contrato.getId(),
                        e.getMessage()));
            }
        }

        // Guardar todos los contratos actualizados en batch
        if (!contratosParaActualizar.isEmpty()) {
            repository.saveAll(contratosParaActualizar);
            System.out.println(String.format(
                    "Actualizados %d contratos: %d VENCIDOS, %d POR VENCER",
                    contratosParaActualizar.size(),
                    contratosVencidos,
                    contratosPorVencer));
        } else {
            System.out.println("No hay contratos que requieran actualización");
        }

        System.out.println("=== Finalizada actualización de estados de contratos ===");
    }
}
