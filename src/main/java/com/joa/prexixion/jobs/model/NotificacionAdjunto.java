package com.joa.prexixion.jobs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sunatBuzonAdjunto")
@Data
@NoArgsConstructor
public class NotificacionAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] contenido;

    @ManyToOne
    @JoinColumn(name = "notificacion_id", nullable = false)
    private Notificacion notificacion;
}
