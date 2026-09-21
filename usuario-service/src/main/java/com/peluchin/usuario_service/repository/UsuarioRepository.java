package com.peluchin.usuario_service.repository;

import com.peluchin.usuario_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCognitoSub(String cognitoSub);

}
