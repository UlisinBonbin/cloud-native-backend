package com.peluchin.admin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeRespuesta {
    private LocalDateTime timestamp;
    private int status;
    private String mensaje;
    private Object datos;

    public static MensajeRespuesta exito(String mensaje, Object datos) {
        return MensajeRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }

    public static MensajeRespuesta creado(String mensaje, Object datos) {
        return MensajeRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(201)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }

    public static MensajeRespuesta error(int status, String mensaje, Object datos) {
        return MensajeRespuesta.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }
}
