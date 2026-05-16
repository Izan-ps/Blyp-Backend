package com.blyp.service;

import com.blyp.dto.AuthResponse;
import com.blyp.dto.LoginRequest;
import com.blyp.dto.RegisterRequest;
import com.blyp.model.Role;
import com.blyp.model.Usuario;
import com.blyp.model.VerificacionEmail;
import com.blyp.repository.UsuarioRepository;
import com.blyp.repository.VerificacionEmailRepository;
import com.blyp.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final VerificacionEmailRepository verificacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthService(UsuarioRepository usuarioRepository,
                       VerificacionEmailRepository verificacionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       EmailService emailService) {
        this.usuarioRepository    = usuarioRepository;
        this.verificacionRepository = verificacionRepository;
        this.passwordEncoder      = passwordEncoder;
        this.jwtService           = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService         = emailService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(Role.ROLE_USER);
        usuario.setVerificado(false);
        usuarioRepository.save(usuario);

        enviarNuevoCodigo(usuario);
    }

    @Transactional
    public AuthResponse verificarEmail(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.isVerificado()) {
            String token = jwtService.generarToken(usuario);
            return new AuthResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.isPro());
        }

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
        usuario.setVerificado(true);
        usuarioRepository.save(usuario);

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.isPro());
    }

    @Transactional
    public void reenviarVerificacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.isVerificado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cuenta ya está verificada");
        }

        enviarNuevoCodigo(usuario);
    }

    @Transactional
    public Object login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!usuario.isVerificado()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no verificada. Revisa tu correo.");
        }

        if (usuario.isHas2fa()) {
            String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
            VerificacionEmail v = new VerificacionEmail();
            v.setUsuario(usuario);
            v.setCodigo(codigo);
            v.setExpiraEn(LocalDateTime.now().plusMinutes(2));
            verificacionRepository.save(v);
            emailService.enviarCodigo2FA(usuario.getEmail(), usuario.getNombre(), codigo);
            return java.util.Map.of("pendiente2fa", true, "email", usuario.getEmail());
        }

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.isPro());
    }

    @Transactional
    public AuthResponse verificar2fa(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        VerificacionEmail v = verificacionRepository
                .findTopByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay código pendiente"));

        if (v.isUsado() || v.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código ha caducado. Inicia sesión de nuevo.");
        }
        if (!v.getCodigo().equals(codigo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorrecto");
        }

        v.setUsado(true);
        verificacionRepository.save(v);

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.isPro());
    }

    private void enviarNuevoCodigo(Usuario usuario) {
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        VerificacionEmail v = new VerificacionEmail();
        v.setUsuario(usuario);
        v.setCodigo(codigo);
        v.setExpiraEn(LocalDateTime.now().plusMinutes(2));
        verificacionRepository.save(v);
        emailService.enviarCodigoVerificacion(usuario.getEmail(), usuario.getNombre(), codigo);
    }
}
