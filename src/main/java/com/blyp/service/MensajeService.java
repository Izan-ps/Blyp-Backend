package com.blyp.service;

import com.blyp.dto.MensajeDto;
import com.blyp.model.Mensaje;
import com.blyp.model.Usuario;
import com.blyp.repository.MensajeRepository;
import com.blyp.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;

    public MensajeService(MensajeRepository mensajeRepository, UsuarioRepository usuarioRepository) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MensajeDto> listar(String email) {
        Usuario usuario = getUsuario(email);
        return mensajeRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId())
                .stream()
                .map(m -> new MensajeDto(m.getId(), m.getTexto(), m.getTipo(), m.getFecha(), m.isLeido()))
                .toList();
    }

    public void eliminar(String email, UUID id) {
        Usuario usuario = getUsuario(email);
        Mensaje mensaje = mensajeRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado"));
        mensajeRepository.delete(mensaje);
    }

    public void crear(Usuario usuario, String texto, String tipo) {
        Mensaje mensaje = new Mensaje();
        mensaje.setUsuario(usuario);
        mensaje.setTexto(texto);
        mensaje.setTipo(tipo);
        mensajeRepository.save(mensaje);
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
