package com.joa.prexixion.jobs.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
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
    private String resultado;      // OK, ERROR, CREDENCIALES_INVALIDAS, etc
    private String mensaje;
    private Long duracionMs;
    private LocalDateTime fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "idJobStatus")
    private JobStatus jobStatus;
}
