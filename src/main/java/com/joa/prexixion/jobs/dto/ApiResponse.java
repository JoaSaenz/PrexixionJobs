package com.joa.prexixion.jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String status; // "OK" o "ERROR"
    private String message; // mensaje descriptivo
    private LocalDateTime timestamp; // marca de tiempo
}
