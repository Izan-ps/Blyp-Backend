package com.blyp.repository;

import com.blyp.model.VerificacionEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface VerificacionEmailRepository extends JpaRepository<VerificacionEmail, UUID> {

    Optional<VerificacionEmail> findTopByUsuarioIdOrderByCreadoEnDesc(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM VerificacionEmail v WHERE v.usuario.id = :usuarioId")
    void deleteByUsuarioId(UUID usuarioId);
}
