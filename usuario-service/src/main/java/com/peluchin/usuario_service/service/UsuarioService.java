package com.peluchin.usuario_service.service;

import com.peluchin.usuario_service.dto.UsuarioRequest;
import com.peluchin.usuario_service.enums.Rol;
import com.peluchin.usuario_service.model.Usuario;
import com.peluchin.usuario_service.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtenerOCrearUsuario(
            String cognitoSub,
            String email,
            String nombre) {

        return usuarioRepository
                .findByCognitoSub(cognitoSub)
                .orElseGet(() -> {

                    Usuario usuario = new Usuario();

                    usuario.setCognitoSub(cognitoSub);
                    usuario.setEmail(email);
                    usuario.setNombre(nombre);
                    usuario.setRol(Rol.USUARIO);

                    return usuarioRepository.save(usuario);
                });
    }

    public Usuario actualizarUsuario(
            String cognitoSub,
            String email,
            String nombre) {

        return usuarioRepository
                .findByCognitoSub(cognitoSub)
                .map(usuario -> {

                    usuario.setNombre(nombre);
                    usuario.setEmail(email);

                    return usuarioRepository.save(usuario);
                })
                .orElse(null);
    }

}
