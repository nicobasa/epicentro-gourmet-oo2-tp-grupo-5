package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Rol;
import com.epicentrogourmet.test.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    private final RolRepository repository;

    public RolService(RolRepository repository) {
        this.repository = repository;
    }

    public Rol agregar(Rol rol) {
        return repository.save(rol);
    }

    public Rol actualizar(Rol rol) {
        if (rol.getId() == null || !repository.existsById(rol.getId())) {
            return null;
        }

        return repository.save(rol);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    public Rol traer(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Rol> traerRoles() {
        return repository.findAll();
    }
}
