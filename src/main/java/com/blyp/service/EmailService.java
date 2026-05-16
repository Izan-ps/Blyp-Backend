package com.blyp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoVerificacion(String destinatario, String nombre, String codigo) {
        log.info(">>> CÓDIGO VERIFICACIÓN para {} : {}", destinatario, codigo);
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(destinatario);
            msg.setSubject("Blyp — Verifica tu cuenta");
            msg.setText(
                "Hola " + nombre + ",\n\n" +
                "Tu código de verificación es:\n\n" +
                "  " + codigo + "\n\n" +
                "Este código caduca en 2 minutos.\n\n" +
                "Si no has creado una cuenta en Blyp, ignora este mensaje.\n\n" +
                "— El equipo de Blyp"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación a {}: {}", destinatario, e.getMessage());
        }
    }

    public void enviarCodigo2FA(String destinatario, String nombre, String codigo) {
        log.info(">>> CÓDIGO 2FA para {} : {}", destinatario, codigo);
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(destinatario);
            msg.setSubject("Blyp — Código de inicio de sesión");
            msg.setText(
                "Hola " + nombre + ",\n\n" +
                "Tu código de verificación para iniciar sesión es:\n\n" +
                "  " + codigo + "\n\n" +
                "Este código caduca en 2 minutos.\n\n" +
                "Si no has intentado iniciar sesión, cambia tu contraseña inmediatamente.\n\n" +
                "— El equipo de Blyp"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Error al enviar email 2FA a {}: {}", destinatario, e.getMessage());
        }
    }

    public void enviarCodigoCambioPassword(String destinatario, String nombre, String codigo) {
        log.info(">>> CÓDIGO CAMBIO PASSWORD para {} : {}", destinatario, codigo);
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(destinatario);
            msg.setSubject("Blyp — Código para cambiar contraseña");
            msg.setText(
                "Hola " + nombre + ",\n\n" +
                "Has solicitado cambiar tu contraseña. Tu código es:\n\n" +
                "  " + codigo + "\n\n" +
                "Este código caduca en 2 minutos.\n\n" +
                "Si no has solicitado este cambio, ignora este mensaje.\n\n" +
                "— El equipo de Blyp"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Error al enviar email de cambio de contraseña a {}: {}", destinatario, e.getMessage());
        }
    }
}
