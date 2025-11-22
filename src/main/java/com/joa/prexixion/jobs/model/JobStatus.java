package com.joa.prexixion.jobs.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreJob; // Ej: "SincronizacionSUNAT"
    private String estado; // Ej: "EN_PROGRESO", "FINALIZADO", "ERROR"
    private Double progreso; // Porcentaje 0.0 a 100.0
    private String mensaje; // Mensaje opcional: "Procesando cliente 3/50"
    private String ultimaActualizacion;

    // Nuevos campos
    private LocalDate fechaEjecucion;   // DATE
    private LocalDateTime horaInicio;   // DATETIME
    private LocalDateTime horaFin;      // DATETIME
    private Integer rucsOk;             // INT
    private Integer rucsNoOk;           // INT

    @OneToMany(mappedBy = "jobStatus", cascade = CascadeType.ALL)
    private List<JobStatusLog> logs;
}