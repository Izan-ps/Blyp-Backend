package com.blyp.config;

import com.blyp.model.Nevera;
import com.blyp.model.Role;
import com.blyp.model.Usuario;
import com.blyp.repository.NeveraRepository;
import com.blyp.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final NeveraRepository neveraRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UsuarioRepository usuarioRepository,
                         NeveraRepository neveraRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.neveraRepository  = neveraRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = "a@gmail.com";
        Role rolDeseado = Role.ROLE_USER;

        usuarioRepository.findByEmail(email).ifPresentOrElse(u -> {
            u.setRole(rolDeseado);
            usuarioRepository.save(u);
            System.out.println(">>> [DEV] Rol actualizado a " + rolDeseado + ": " + email);
        }, () -> {
            Usuario u = new Usuario();
            u.setNombre("Izan");
            u.setEmail(email);
            u.setPasswordHash(passwordEncoder.encode("Bonifasi04#"));
            u.setRole(rolDeseado);
            u.setVerificado(true);
            usuarioRepository.save(u);

            Nevera nevera = new Nevera();
            nevera.setUsuario(u);
            neveraRepository.save(nevera);

            System.out.println(">>> [DEV] Usuario de prueba creado: " + email);
        });
    }
}
