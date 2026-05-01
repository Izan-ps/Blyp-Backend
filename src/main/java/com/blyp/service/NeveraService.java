package com.blyp.service;

import com.blyp.dto.ProductoNeveraDto;
import com.blyp.dto.ProductoRequest;
import com.blyp.model.Nevera;
import com.blyp.model.ProductoNevera;
import com.blyp.model.Usuario;
import com.blyp.repository.NeveraRepository;
import com.blyp.repository.ProductoNeveraRepository;
import com.blyp.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class NeveraService {

    private final NeveraRepository neveraRepository;
    private final ProductoNeveraRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public NeveraService(NeveraRepository neveraRepository,
                         ProductoNeveraRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.neveraRepository  = neveraRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository  = usuarioRepository;
    }

    public List<ProductoNeveraDto> listarProductos(String email) {
        Nevera nevera = obtenerOCrearNevera(email);
        return productoRepository.findByNeveraId(nevera.getId())
                .stream()
                .map(p -> new ProductoNeveraDto(p.getId(), p.getNombre(), p.getCantidad(), p.getCategoria(), p.getCodigoBarras()))
                .toList();
    }

    @Transactional
    public ProductoNeveraDto añadirProducto(String email, ProductoRequest request) {
        Nevera nevera = obtenerOCrearNevera(email);

        java.util.Optional<ProductoNevera> existente = productoRepository
                .findByNeveraIdAndNombreIgnoreCase(nevera.getId(), request.getNombre());

        if (existente.isPresent()) {
            ProductoNevera producto = existente.get();
            producto.setCantidad(producto.getCantidad() + request.getCantidad());
            ProductoNevera guardado = productoRepository.save(producto);
            return new ProductoNeveraDto(guardado.getId(), guardado.getNombre(), guardado.getCantidad(), guardado.getCategoria(), guardado.getCodigoBarras());
        }

        ProductoNevera producto = new ProductoNevera();
        producto.setNevera(nevera);
        producto.setNombre(request.getNombre());
        producto.setCantidad(request.getCantidad());
        producto.setCategoria(request.getCategoria());
        producto.setCodigoBarras(request.getCodigoBarras());

        ProductoNevera guardado = productoRepository.save(producto);
        return new ProductoNeveraDto(guardado.getId(), guardado.getNombre(), guardado.getCantidad(), guardado.getCategoria(), guardado.getCodigoBarras());
    }

    @Transactional
    public ProductoNeveraDto editarProducto(String email, UUID productoId, ProductoRequest request) {
        ProductoNevera producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        verificarPropietario(producto, email);

        if (request.getNombre() != null) producto.setNombre(request.getNombre());
        if (request.getCategoria() != null) producto.setCategoria(request.getCategoria());
        producto.setCantidad(request.getCantidad());

        ProductoNevera guardado = productoRepository.save(producto);
        return new ProductoNeveraDto(guardado.getId(), guardado.getNombre(), guardado.getCantidad(), guardado.getCategoria(), guardado.getCodigoBarras());
    }

    public java.util.Optional<ProductoNeveraDto> buscarPorCodigoBarras(String email, String codigo) {
        Nevera nevera = obtenerOCrearNevera(email);
        return productoRepository.findByNeveraIdAndCodigoBarras(nevera.getId(), codigo)
                .map(p -> new ProductoNeveraDto(p.getId(), p.getNombre(), p.getCantidad(), p.getCategoria(), p.getCodigoBarras()));
    }

    @Transactional
    public void eliminarProducto(String email, UUID productoId) {
        ProductoNevera producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        verificarPropietario(producto, email);
        productoRepository.delete(producto);
    }

    private Nevera obtenerOCrearNevera(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        return neveraRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Nevera nueva = new Nevera();
                    nueva.setUsuario(usuario);
                    return neveraRepository.save(nueva);
                });
    }

    private void verificarPropietario(ProductoNevera producto, String email) {
        String emailPropietario = producto.getNevera().getUsuario().getEmail();
        if (!emailPropietario.equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este producto");
        }
    }
}
