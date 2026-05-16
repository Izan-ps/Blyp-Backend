package com.blyp.service;

import com.blyp.dto.PerfilDto;
import com.blyp.model.Usuario;
import com.blyp.model.VerificacionEmail;
import com.blyp.repository.UsuarioRepository;
import com.blyp.repository.VerificacionEmailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class CuentaService {

    private final UsuarioRepository usuarioRepository;
    private final VerificacionEmailRepository verificacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    public CuentaService(UsuarioRepository usuarioRepository,
                         VerificacionEmailRepository verificacionRepository,
                         PasswordEncoder passwordEncoder,
                         EmailService emailService) {
        this.usuarioRepository    = usuarioRepository;
        this.verificacionRepository = verificacionRepository;
        this.passwordEncoder      = passwordEncoder;
        this.emailService         = emailService;
    }

    public PerfilDto getPerfil(String email) {
        Usuario u = getUsuario(email);
        return new PerfilDto(u.getNombre(), u.getEmail(), u.getCreatedAt(), u.isPro(), u.isHas2fa());
    }

    @Transactional
    public boolean toggle2fa(String email) {
        Usuario usuario = getUsuario(email);
        usuario.setHas2fa(!usuario.isHas2fa());
        usuarioRepository.save(usuario);
        return usuario.isHas2fa();
    }

    @Transactional
    public void solicitarCambioPassword(String email) {
        Usuario usuario = getUsuario(email);
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        VerificacionEmail v = new VerificacionEmail();
        v.setUsuario(usuario);
        v.setCodigo(codigo);
        v.setExpiraEn(LocalDateTime.now().plusMinutes(2));
        verificacionRepository.save(v);
        emailService.enviarCodigoCambioPassword(email, usuario.getNombre(), codigo);
    }

    @Transactional
    public void cambiarPassword(String email, String codigo, String nuevaPassword) {
        Usuario usuario = getUsuario(email);

        VerificacionEmail v = verificacionRepository
                .findTopByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay código pendiente"));

        if (v.isUsado() || v.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código ha caducado. Solicita uno nuevo.");
        }
        if (!v.getCodigo().equals(codigo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorrecto");
        }

        v.setUsado(true);
        verificacionRepository.save(v);
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarCuenta(String email) {
        Usuario usuario = getUsuario(email);
        verificacionRepository.deleteByUsuarioId(usuario.getId());
        usuarioRepository.delete(usuario);
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
