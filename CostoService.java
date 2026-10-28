package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Costo;
import com.epicentrogourmet.test.repository.CostoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CostoService {

    private final CostoRepository repository;

    public CostoService(CostoRepository repository) {
        this.repository = repository;
    }

    public Costo agregar(Costo costo) {
        return repository.save(costo);
    }

    public Costo actualizar(Costo costo) {
        if (costo.getId() == null || !repository.existsById(costo.getId())) {
            return null;
        }

        return repository.save(costo);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    public Costo traer(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Costo> traerCostos() {
        return repository.findAll();
    }
}
