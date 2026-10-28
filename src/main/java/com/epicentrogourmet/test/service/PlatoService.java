package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Plato;
import com.epicentrogourmet.test.repository.PlatoRepository;
import com.epicentrogourmet.test.repository.UnidadVentaRepository;
import com.epicentrogourmet.test.entity.UnidadVenta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatoService {

    private final PlatoRepository repository;
    private final UnidadVentaRepository unidades;
    private final com.epicentrogourmet.test.repository.DetallePedidoRepository detalles;

    public PlatoService(PlatoRepository repository, UnidadVentaRepository unidades,
                        com.epicentrogourmet.test.repository.DetallePedidoRepository detalles) {
        this.repository = repository;
        this.unidades = unidades;
        this.detalles = detalles;
    }

    public Plato agregar(Plato objeto) {
        return repository.save(objeto);
    }

    public void actualizar(Plato objeto) {
        repository.save(objeto);
    }

    public void eliminar(Plato objeto) {
        repository.delete(objeto);
    }

    public Plato traer(Long idPlato) {
        return repository.findById(idPlato).orElse(null);
    }

    public List<Plato> traer() {
        return repository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public Plato agregarAUnidad(Long unidadId, String nombre, Double precio, Double costo) {
        UnidadVenta unidad = unidades.findById(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        validar(nombre, precio, costo);
        Plato plato = new Plato();
        plato.setNombre(nombre.trim());
        plato.setPrecioVenta(precio);
        plato.setCostoProduccion(costo);
        repository.save(plato);
        unidad.getPlatos().add(plato);
        unidades.save(unidad);
        return plato;
    }

    @org.springframework.transaction.annotation.Transactional
    public Plato modificarDeUnidad(Long unidadId, Long platoId, String nombre, Double precio, Double costo) {
        UnidadVenta unidad = unidades.findById(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        Plato plato = unidad.getPlatos().stream().filter(p -> p.getId().equals(platoId)).findFirst()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("El plato no pertenece a tu unidad."));
        validar(nombre, precio, costo);
        if (nombre.trim().equals(plato.getNombre()) && Double.compare(precio, plato.getPrecioVenta()) == 0
                && Double.compare(costo, plato.getCostoProduccion()) == 0) return plato;
        // Solo se separa la referencia cuando hay otra unidad o ventas que necesitan conservar el original.
        if (unidades.countDistinctByPlatosId(platoId) > 1 || detalles.existsByPlatoId(platoId)) {
            Plato propio = new Plato();
            propio.setNombre(nombre.trim()); propio.setPrecioVenta(precio); propio.setCostoProduccion(costo);
            repository.save(propio);
            unidad.getPlatos().removeIf(p -> platoId.equals(p.getId()));
            unidad.getPlatos().add(propio);
            unidades.save(unidad);
            return propio;
        }
        plato.setNombre(nombre.trim());
        plato.setPrecioVenta(precio);
        plato.setCostoProduccion(costo);
        return repository.save(plato);
    }

    @org.springframework.transaction.annotation.Transactional
    public void bajaDeUnidad(Long unidadId, Long platoId) {
        UnidadVenta unidad = unidades.findById(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        Plato plato = unidad.getPlatos().stream().filter(p -> p.getId().equals(platoId)).findFirst()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("El plato no pertenece a tu unidad."));
        unidad.getPlatos().remove(plato);
        unidades.save(unidad);
    }

    private static void validar(String nombre, Double precio, Double costo) {
        if (nombre == null || nombre.isBlank() || precio == null || !Double.isFinite(precio) || precio <= 0
                || costo == null || !Double.isFinite(costo) || costo < 0 || costo > precio) {
            throw new IllegalArgumentException("Nombre, precio y costo deben ser válidos; costo no puede superar el precio.");
        }
    }
}
