package com.peluchin.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoRequest {

    private String nombre;
    private BigDecimal precio;
    private String imagenUrl;
    private Integer stock;
}
