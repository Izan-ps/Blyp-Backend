package com.blyp.service;

import com.blyp.dto.ItemListaDto;
import com.blyp.dto.ItemRequest;
import com.blyp.dto.ListaDto;
import com.blyp.dto.ListaRequest;
import com.blyp.model.ItemLista;
import com.blyp.model.Lista;
import com.blyp.model.Usuario;
import com.blyp.repository.ItemListaRepository;
import com.blyp.repository.ListaRepository;
import com.blyp.repository.NeveraRepository;
import com.blyp.repository.ProductoNeveraRepository;
import com.blyp.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final ItemListaRepository itemListaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NeveraRepository neveraRepository;
    private final ProductoNeveraRepository productoNeveraRepository;

    public ListaService(ListaRepository listaRepository,
                        ItemListaRepository itemListaRepository,
                        UsuarioRepository usuarioRepository,
                        NeveraRepository neveraRepository,
                        ProductoNeveraRepository productoNeveraRepository) {
        this.listaRepository          = listaRepository;
        this.itemListaRepository      = itemListaRepository;
        this.usuarioRepository        = usuarioRepository;
        this.neveraRepository         = neveraRepository;
        this.productoNeveraRepository = productoNeveraRepository;
    }

    public List<ListaDto> listar(String email) {
        Usuario usuario = getUsuario(email);
        return listaRepository.findByUsuarioIdOrderByUpdatedAtDesc(usuario.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ListaDto crear(String email, ListaRequest request) {
        Usuario usuario = getUsuario(email);
        Lista lista = new Lista();
        lista.setUsuario(usuario);
        lista.setNombre(request.getNombre());
        lista = listaRepository.save(lista);
        guardarItems(lista, request.getItems());
        return toDto(lista);
    }

    @Transactional
    public ListaDto editar(String email, UUID id, ListaRequest request) {
        Lista lista = listaRepository.findByIdAndUsuarioId(id, getUsuario(email).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
        lista.setNombre(request.getNombre());
        itemListaRepository.deleteByListaId(lista.getId());
        guardarItems(lista, request.getItems());
        return toDto(listaRepository.save(lista));
    }

    @Transactional
    public void eliminar(String email, UUID id) {
        Lista lista = listaRepository.findByIdAndUsuarioId(id, getUsuario(email).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
        itemListaRepository.deleteByListaId(lista.getId());
        listaRepository.delete(lista);
    }

    public List<Map<String, Object>> listaAuto(String email) {
        Usuario usuario = getUsuario(email);
        return neveraRepository.findByUsuarioId(usuario.getId())
                .map(nevera -> productoNeveraRepository.findByNeveraId(nevera.getId())
                        .stream()
                        .filter(p -> p.getStockMinimo() != null && p.getCantidad() <= p.getStockMinimo())
                        .map(p -> Map.<String, Object>of(
                                "nombre", p.getNombre(),
                                "cantidad", p.getStockMinimo() - p.getCantidad() + 1
                        ))
                        .toList())
                .orElse(List.of());
    }

    private void guardarItems(Lista lista, List<ItemRequest> items) {
        for (ItemRequest req : items) {
            ItemLista item = new ItemLista();
            item.setLista(lista);
            item.setNombre(req.getNombre());
            item.setCantidad(req.getCantidad());
            itemListaRepository.save(item);
        }
    }

    private ListaDto toDto(Lista lista) {
        List<ItemListaDto> items = itemListaRepository.findByListaId(lista.getId())
                .stream()
                .map(i -> new ItemListaDto(i.getId(), i.getNombre(), i.getCantidad()))
                .toList();
        return new ListaDto(lista.getId(), lista.getNombre(), items, lista.getCreatedAt(), lista.getUpdatedAt());
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
