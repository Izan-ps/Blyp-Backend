package com.blyp.repository;

import com.blyp.model.ItemLista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemListaRepository extends JpaRepository<ItemLista, UUID> {
    List<ItemLista> findByListaId(UUID listaId);
    void deleteByListaId(UUID listaId);
}
