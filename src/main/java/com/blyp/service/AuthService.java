package com.blyp.service;

import com.blyp.dto.AuthResponse;
import com.blyp.dto.LoginRequest;
import com.blyp.dto.RegisterRequest;
import com.blyp.model.Role;
import com.blyp.model.Usuario;
import com.blyp.repository.UsuarioRepository;
import com.blyp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
        this.jwtService        = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(Role.ROLE_USER);
        usuarioRepository.save(usuario);

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.isPro());
    }

    public AuthResponse login(LoginRequest request) {
        // Lanza BadCredentialsException si las credenciales son incorrectas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.isPro());
    }
}
