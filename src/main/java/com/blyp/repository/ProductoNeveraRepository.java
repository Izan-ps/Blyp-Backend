package com.blyp.repository;

import com.blyp.model.ProductoNevera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductoNeveraRepository extends JpaRepository<ProductoNevera, UUID> {
    List<ProductoNevera> findByNeveraId(UUID neveraId);
    java.util.Optional<ProductoNevera> findByNeveraIdAndCodigoBarras(UUID neveraId, String codigoBarras);
    java.util.Optional<ProductoNevera> findByNeveraIdAndNombreIgnoreCase(UUID neveraId, String nombre);
}
