package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.DetallePedido;
import com.epicentrogourmet.test.repository.DetallePedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetallePedidoService {

    private final DetallePedidoRepository repository;

    public DetallePedidoService(DetallePedidoRepository repository) {
        this.repository = repository;
    }

    public DetallePedido agregar(DetallePedido objeto) {
        return repository.save(objeto);
    }

    public void actualizar(DetallePedido objeto) {
        repository.save(objeto);
    }

    public void eliminar(DetallePedido objeto) {
        repository.delete(objeto);
    }

    public DetallePedido traer(Long idDetallePedido) {
        return repository.findById(idDetallePedido).orElse(null);
    }

    public List<DetallePedido> traer() {
        return repository.findAll();
    }
}