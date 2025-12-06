package com.joa.prexixion.jobs.dto;

import java.util.List;

import lombok.Data;

@Data
public class SunatBuzonResponseDTO {
    private boolean success;
    private String type;
    private String message;
    private List<NotificacionDTO> notificaciones;
}
