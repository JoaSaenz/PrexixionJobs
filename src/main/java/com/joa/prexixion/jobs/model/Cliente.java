package com.joa.prexixion.jobs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")  
public class Cliente {
    @Id
    @Column(length = 11, nullable = false)
    private String ruc;
    
    @Column(length = 50)
    private String solU;
    @Column(length = 50)
    private String solC;
    
    public Cliente() {
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getSolU() {
        return solU;
    }

    public void setSolU(String solU) {
        this.solU = solU;
    }

    public String getSolC() {
        return solC;
    }

    public void setSolC(String solC) {
        this.solC = solC;
    }
    
}
