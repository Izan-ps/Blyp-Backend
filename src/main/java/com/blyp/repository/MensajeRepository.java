package com.blyp.repository;

import com.blyp.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensajeRepository extends JpaRepository<Mensaje, UUID> {
    List<Mensaje> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);
    Optional<Mensaje> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
