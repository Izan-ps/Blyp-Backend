package com.blyp.repository;

import com.blyp.model.Lista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListaRepository extends JpaRepository<Lista, UUID> {
    List<Lista> findByUsuarioIdOrderByUpdatedAtDesc(UUID usuarioId);
    Optional<Lista> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
