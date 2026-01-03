package com.joa.prexixion.jobs.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobStatusLog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruc;
    private String y;
    private String resultado; // OK, ERROR, CREDENCIALES_INVALIDAS, etc
    private String mensaje;
    private Long duracionMs;
    private LocalDateTime fechaRegistro;
    private Integer nuevasNotificaciones; // cantidad de nuevas notificaciones para ese RUC

    @ManyToOne
    @JoinColumn(name = "jobStatusId")
    private JobStatus jobStatus;
}
