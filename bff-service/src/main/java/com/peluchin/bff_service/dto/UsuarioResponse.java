package com.peluchin.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String cognitoSub;
    private String nombre;
    private String email;
    private String rol;
}
