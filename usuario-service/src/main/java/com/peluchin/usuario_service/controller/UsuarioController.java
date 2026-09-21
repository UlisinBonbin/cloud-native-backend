package com.peluchin.usuario_service.controller;

import com.peluchin.usuario_service.dto.UsuarioRequest;
import com.peluchin.usuario_service.model.Usuario;
import com.peluchin.usuario_service.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public Usuario getUsuarioActual(
            @AuthenticationPrincipal Jwt jwt) {

        return usuarioService.getUsuarioActual(
                jwt.getSubject()
        );
    }

    @PostMapping("/me")
    public Usuario crearUsuario(
            @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return usuarioService.crearUsuario(
                jwt.getSubject(),
                request
        );
    }

    @PutMapping("/me")
    public Usuario actualizarUsuario(
            @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return usuarioService.actualizarUsuario(
                jwt.getSubject(),
                request
        );
    }

}
