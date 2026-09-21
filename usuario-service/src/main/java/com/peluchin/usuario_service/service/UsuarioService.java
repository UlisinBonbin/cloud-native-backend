package com.peluchin.usuario_service.service;

import com.peluchin.usuario_service.dto.UsuarioRequest;
import com.peluchin.usuario_service.model.Usuario;
import com.peluchin.usuario_service.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuarioActual(String cognitoSub) {

        return usuarioRepository
                .findByCognitoSub(cognitoSub)
                .orElse(null);
    }

    public Usuario crearUsuario(
            String cognitoSub,
            UsuarioRequest request) {

        Usuario usuario = new Usuario();

        usuario.setCognitoSub(cognitoSub);
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());

        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(
            String cognitoSub,
            UsuarioRequest request) {

        return usuarioRepository
                .findByCognitoSub(cognitoSub)
                .map(usuario -> {

                    usuario.setNombre(request.getNombre());
                    usuario.setEmail(request.getEmail());

                    return usuarioRepository.save(usuario);
                })
                .orElse(null);
    }

}
