package com.joa.prexixion.jobs.dto;

import java.util.List;


public class SunatBuzonResponseDTO {
    private boolean success;
    private String message;
    private List<NotificacionDTO> notificaciones;
    public SunatBuzonResponseDTO() {
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public List<NotificacionDTO> getNotificaciones() {
        return notificaciones;
    }
    public void setNotificaciones(List<NotificacionDTO> notificaciones) {
        this.notificaciones = notificaciones;
    }

    
    
}
