package com.epicentrogourmet.test.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remitente;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void enviarCredencialesEmpleado(String destinatario, String nombreCompleto, String passwordTemporal) {
        String cuerpo = "Hola " + nombreCompleto + ",\n\n"
                + "Tu cuenta en Epicentro Gourmet ya está habilitada.\n"
                + "Usuario: " + destinatario + "\n"
                + "Contraseña temporal: " + passwordTemporal + "\n\n"
                + "Iniciá sesión en http://localhost:8080/login y abrí Ajustes de mi cuenta para elegir tu propia contraseña.";

        if (mailSender == null || remitente == null || remitente.isBlank() || "correo".equalsIgnoreCase(remitente.trim())) {
            throw new IllegalStateException("No se pudo enviar el correo: configurá SMTP antes de dar de alta al empleado. No se guardaron cambios.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remitente);
        message.setTo(destinatario);
        message.setSubject("Acceso a Epicentro Gourmet");
        message.setText(cuerpo);
        try {
            mailSender.send(message);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("No se pudo enviar el correo de acceso. Revisá SMTP e intentá de nuevo. No se guardaron cambios.");
        }
    }
}
