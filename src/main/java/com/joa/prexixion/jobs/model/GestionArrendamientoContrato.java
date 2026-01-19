package com.joa.prexixion.jobs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "gestionArrendamientosContratos")
@Data
public class GestionArrendamientoContrato {

    @Id
    private Integer id;

    private String fechaFin; // formato yyyy-MM-dd

    private String estadoLogico; // VENCIDO, VIGENTE, POR VENCER
}
