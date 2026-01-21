package com.joa.prexixion.jobs.dto;

import java.util.List;

public class NotificacionDTO {
    private String id;
    private String titulo;
    private String fecha;
    private List<AdjuntoDTO> adjuntos;

    public NotificacionDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public List<AdjuntoDTO> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<AdjuntoDTO> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public static class AdjuntoDTO {
        private String nombre;
        private String base64;

        public AdjuntoDTO() {
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getBase64() {
            return base64;
        }

        public void setBase64(String base64) {
            this.base64 = base64;
        }
    }
}
