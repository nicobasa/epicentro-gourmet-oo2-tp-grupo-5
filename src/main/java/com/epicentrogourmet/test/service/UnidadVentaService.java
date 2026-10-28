package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.UnidadVenta;
import com.epicentrogourmet.test.repository.UnidadVentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadVentaService {

    private final UnidadVentaRepository repository;

    public UnidadVentaService(UnidadVentaRepository repository) {
        this.repository = repository;
    }

    public UnidadVenta agregar(UnidadVenta objeto) {
        return repository.save(objeto);
    }

    public void actualizar(UnidadVenta objeto) {
        repository.save(objeto);
    }

    public void eliminar(UnidadVenta objeto) {
        repository.delete(objeto);
    }

    public UnidadVenta traer(Long idUnidadVenta) {
        return repository.findById(idUnidadVenta).orElse(null);
    }

    public List<UnidadVenta> traer() {
        return repository.findAll();
    }
}