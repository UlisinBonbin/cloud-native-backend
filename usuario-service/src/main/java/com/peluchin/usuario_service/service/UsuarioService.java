package com.peluchin.usuario_service.service;

import com.peluchin.usuario_service.dto.UsuarioRequest;
import com.peluchin.usuario_service.enums.Rol;
import com.peluchin.usuario_service.model.Usuario;
import com.peluchin.usuario_service.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RestClient restClient;

    @Value("${cognito.userinfo-uri}")
    private String userInfoUri;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.restClient = RestClient.create();
    }

    public Usuario obtenerOCrearUsuario(
            String cognitoSub,
            String accessToken) {

        // Obtener información real del usuario desde Cognito
        Map<String, Object> userInfo = restClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String email = (String) userInfo.get("email");
        String nombre = (String) userInfo.get("name");

        // Valores de respaldo
        if (nombre == null || nombre.isBlank()) {
            nombre = (String) userInfo.get("preferred_username");
        }

        if (nombre == null || nombre.isBlank()) {
            nombre = "Usuario";
        }

        if (email == null || email.isBlank()) {
            email = cognitoSub;
        }

        String finalEmail = email;
        String finalNombre = nombre;

        return usuarioRepository
                .findByCognitoSub(cognitoSub)
                .map(usuario -> {

                    // El usuario ya existe:
                    // actualizamos sus datos de Cognito.
                    usuario.setEmail(finalEmail);
                    usuario.setNombre(finalNombre);

                    // NO modificamos el rol.
                    // Si algún día es ADMINISTRADOR,
                    // no queremos convertirlo nuevamente en USUARIO.

                    return usuarioRepository.save(usuario);
                })
                .orElseGet(() -> {

                    // El usuario no existe:
                    // lo creamos como USUARIO.
                    Usuario usuario = new Usuario();

                    usuario.setCognitoSub(cognitoSub);
                    usuario.setEmail(finalEmail);
                    usuario.setNombre(finalNombre);
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
