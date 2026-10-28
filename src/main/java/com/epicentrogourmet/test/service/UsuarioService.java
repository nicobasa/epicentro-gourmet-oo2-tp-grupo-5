package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Usuario;
import com.epicentrogourmet.test.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario agregar(Usuario usuario) {
        return repository.save(usuario);
    }

    public Usuario traer(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Usuario traerPorUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }

    public List<Usuario> traer() {
        return repository.findAll();
    }
}