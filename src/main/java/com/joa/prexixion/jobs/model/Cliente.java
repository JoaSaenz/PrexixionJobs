package com.joa.prexixion.jobs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")  
public class Cliente {
    @Id
    private String ruc;
    
    private String solU;
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
