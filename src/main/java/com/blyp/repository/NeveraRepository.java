package com.blyp.repository;

import com.blyp.model.Nevera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NeveraRepository extends JpaRepository<Nevera, UUID> {
    Optional<Nevera> findByUsuarioId(UUID usuarioId);
}
