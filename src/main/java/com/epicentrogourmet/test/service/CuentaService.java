package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service
public class CuentaService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public CuentaService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional
    public void cambiarPassword(String username, String actual, String nueva, String confirmacion) {
        var usuario = usuarios.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta no existe."));
        if (actual == null || !encoder.matches(actual, usuario.getPassword()))
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        if (nueva == null || nueva.length() < 8 || nueva.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres y hasta 72 bytes.");
        if (!nueva.equals(confirmacion)) throw new IllegalArgumentException("Las contraseñas nuevas no coinciden.");
        if (encoder.matches(nueva, usuario.getPassword()))
            throw new IllegalArgumentException("Elegí una contraseña diferente de la actual.");
        usuario.setPassword(encoder.encode(nueva));
        usuarios.save(usuario);
    }
}
