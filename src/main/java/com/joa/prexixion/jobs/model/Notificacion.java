package com.joa.prexixion.jobs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "sunatBuzonNotificacion", uniqueConstraints = @UniqueConstraint(columnNames = { "ruc", "idSunat" }))
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 11, nullable = false)
    private String ruc;

    @Column(length = 50, nullable = false)
    private String idSunat;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String titulo;
    private LocalDateTime fecha;

    @Column(name = "jobStatusId")
    private Long jobStatusId;

    @OneToMany(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificacionAdjunto> adjuntos = new ArrayList<>();

    public Notificacion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getIdSunat() {
        return idSunat;
    }

    public void setIdSunat(String idSunat) {
        this.idSunat = idSunat;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Long getJobStatusId() {
        return jobStatusId;
    }

    public void setJobStatusId(Long jobStatusId) {
        this.jobStatusId = jobStatusId;
    }

    public List<NotificacionAdjunto> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<NotificacionAdjunto> adjuntos) {
        this.adjuntos = adjuntos;
    }

}
