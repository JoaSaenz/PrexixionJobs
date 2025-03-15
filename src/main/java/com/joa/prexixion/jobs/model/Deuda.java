package com.joa.prexixion.jobs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "deudasNew")
public class Deuda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Indica que la PK es autoincremental
    private int id; // Cambiado a int para coincidir con la BD

    private int idEstado;

    @Column(name = "fVencimiento")
    private String fVencimiento;

    private String idCliente;

    private int idEstadoPago;

    // Constructor vacío
    public Deuda() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    public String getfVencimiento() {
        return fVencimiento;
    }

    public void setfVencimiento(String fVencimiento) {
        this.fVencimiento = fVencimiento;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdEstadoPago() {
        return idEstadoPago;
    }

    public void setIdEstadoPago(int idEstadoPago) {
        this.idEstadoPago = idEstadoPago;
    }

    @Override
    public String toString() {
        return "Deuda [id=" + id + ", idEstado=" + idEstado + ", fVencimiento=" + fVencimiento + ", idCliente="
                + idCliente + ", idEstadoPago=" + idEstadoPago + "]";
    }

}
