package com.peluchin.bff_service.controller;

import com.peluchin.bff_service.client.UsuarioClient;
import com.peluchin.bff_service.dto.UsuarioRequest;
import com.peluchin.bff_service.dto.UsuarioResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    private final UsuarioClient usuarioClient;

    public UsuarioController(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    @GetMapping("/me")
    public UsuarioResponse obtenerUsuarioActual() {
        return usuarioClient.obtenerUsuarioActual();
    }

    @PostMapping("/me")
    public UsuarioResponse crearUsuario(
            @RequestBody UsuarioRequest request) {

        return usuarioClient.crearUsuario(request);
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarUsuario(
            @RequestBody UsuarioRequest request) {

        return usuarioClient.actualizarUsuario(request);
    }
}
