package com.blyp.service;

import com.blyp.dto.ProductoNeveraDto;
import com.blyp.dto.ProductoRequest;
import com.blyp.model.Nevera;
import com.blyp.model.ProductoNevera;
import com.blyp.model.Usuario;
import com.blyp.repository.NeveraRepository;
import com.blyp.repository.ProductoNeveraRepository;
import com.blyp.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NeveraService {

    private final NeveraRepository neveraRepository;
    private final ProductoNeveraRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MensajeService mensajeService;

    public NeveraService(NeveraRepository neveraRepository,
                         ProductoNeveraRepository productoRepository,
                         UsuarioRepository usuarioRepository,
                         MensajeService mensajeService) {
        this.neveraRepository  = neveraRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository  = usuarioRepository;
        this.mensajeService    = mensajeService;
    }

    public List<ProductoNeveraDto> listarProductos(String email) {
        Nevera nevera = obtenerOCrearNevera(email);
        return productoRepository.findByNeveraId(nevera.getId())
                .stream()
                .map(p -> new ProductoNeveraDto(p.getId(), p.getNombre(), p.getCantidad(), p.getCategoria(), p.getCodigoBarras(), p.getStockMinimo()))
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
            return new ProductoNeveraDto(guardado.getId(), guardado.getNombre(), guardado.getCantidad(), guardado.getCategoria(), guardado.getCodigoBarras(), guardado.getStockMinimo());
        }

        ProductoNevera producto = new ProductoNevera();
        producto.setNevera(nevera);
        producto.setNombre(request.getNombre());
        producto.setCantidad(request.getCantidad());
        producto.setCategoria(request.getCategoria());
        producto.setCodigoBarras(request.getCodigoBarras());

        ProductoNevera guardado = productoRepository.save(producto);
        return new ProductoNeveraDto(guardado.getId(), guardado.getNombre(), guardado.getCantidad(), guardado.getCategoria(), guardado.getCodigoBarras(), guardado.getStockMinimo());
    }

    @Transactional
    public ProductoNeveraDto editarProducto(String email, UUID productoId, ProductoRequest request) {
        ProductoNevera producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        verificarPropietario(producto, email);

        Integer cantidadAnterior = producto.getCantidad();
        if (request.getNombre() != null) producto.setNombre(request.getNombre());
        if (request.getCategoria() != null) producto.setCategoria(request.getCategoria());
        producto.setCantidad(request.getCantidad());
        producto.setStockMinimo(request.getStockMinimo());

        ProductoNevera guardado = productoRepository.save(producto);

        Integer minimo = guardado.getStockMinimo();
        if (minimo != null && guardado.getCantidad() <= minimo && guardado.getCantidad() < cantidadAnterior) {
            Usuario usuario = guardado.getNevera().getUsuario();
            String texto = "Stock bajo: solo " + guardado.getCantidad() + " ud"
                    + (guardado.getCantidad() != 1 ? "s" : "") + " de " + guardado.getNombre() + ".";
            mensajeService.crear(usuario, texto, "warning");
        }

        return new ProductoNeveraDto(guardado.getId(), guardado.getNombre(), guardado.getCantidad(), guardado.getCategoria(), guardado.getCodigoBarras(), guardado.getStockMinimo());
    }

    public java.util.Optional<ProductoNeveraDto> buscarPorCodigoBarras(String email, String codigo) {
        Nevera nevera = obtenerOCrearNevera(email);
        return productoRepository.findByNeveraIdAndCodigoBarras(nevera.getId(), codigo)
                .map(p -> new ProductoNeveraDto(p.getId(), p.getNombre(), p.getCantidad(), p.getCategoria(), p.getCodigoBarras(), p.getStockMinimo()));
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

        Optional<Nevera> existente = neveraRepository.findByUsuarioId(usuario.getId());
        if (existente.isPresent()) return existente.get();

        try {
            Nevera nueva = new Nevera();
            nueva.setUsuario(usuario);
            return neveraRepository.saveAndFlush(nueva);
        } catch (DataIntegrityViolationException e) {
            return neveraRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear la nevera"));
        }
    }

    private void verificarPropietario(ProductoNevera producto, String email) {
        String emailPropietario = producto.getNevera().getUsuario().getEmail();
        if (!emailPropietario.equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este producto");
        }
    }
}
