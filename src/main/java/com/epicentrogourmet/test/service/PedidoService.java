package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Pedido;
import com.epicentrogourmet.test.repository.PedidoRepository;
import com.epicentrogourmet.test.repository.FestivalRepository;
import com.epicentrogourmet.test.repository.UnidadVentaRepository;
import com.epicentrogourmet.test.repository.PlatoRepository;
import com.epicentrogourmet.test.repository.DetallePedidoRepository;
import com.epicentrogourmet.test.entity.Empleado;
import com.epicentrogourmet.test.entity.Festival;
import com.epicentrogourmet.test.entity.Plato;
import com.epicentrogourmet.test.entity.UnidadVenta;
import com.epicentrogourmet.test.entity.DetallePedido;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final FestivalRepository festivales;
    private final UnidadVentaRepository unidades;
    private final PlatoRepository platos;
    private final DetallePedidoRepository detalles;
    private final EmpleadoService empleadoService;
    private final com.epicentrogourmet.test.repository.CierreCajaRepository cierres;

    public PedidoService(PedidoRepository repository, FestivalRepository festivales, UnidadVentaRepository unidades,
                         PlatoRepository platos, DetallePedidoRepository detalles, EmpleadoService empleadoService,
                         com.epicentrogourmet.test.repository.CierreCajaRepository cierres) {
        this.repository = repository;
        this.festivales = festivales;
        this.unidades = unidades;
        this.platos = platos;
        this.detalles = detalles;
        this.empleadoService = empleadoService;
        this.cierres = cierres;
    }

    public Pedido agregar(Pedido objeto) {
        return repository.save(objeto);
    }

    public void actualizar(Pedido objeto) {
        repository.save(objeto);
    }

    public void eliminar(Pedido objeto) {
        repository.delete(objeto);
    }

    public Pedido traer(Long idPedido) {
        return repository.findById(idPedido).orElse(null);
    }

    public List<Pedido> traer() {
        return repository.findAll();
    }

    public List<Pedido> porUnidad(Long unidadId) { return repository.findByUnidadDeVentaIdOrderByFechaTransaccionDesc(unidadId); }

    @Transactional
    public Pedido registrar(Long festivalId, Long unidadId, Long plato1Id, Integer cantidad1,
                            Long plato2Id, Integer cantidad2, String username, boolean administrador) {
        Festival festival = festivales.findById(festivalId).orElseThrow(() -> new IllegalArgumentException("Festival inexistente."));
        UnidadVenta unidad = unidades.bloquearPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        if (!unidad.getFestivales().stream().anyMatch(f -> f.getId().equals(festivalId))) {
            throw new IllegalArgumentException("La unidad no participa en el festival seleccionado.");
        }
        LocalDate hoy = LocalDate.now();
        if (festival.getFechaInicio() == null || festival.getFechaFin() == null
                || hoy.isBefore(festival.getFechaInicio()) || hoy.isAfter(festival.getFechaFin())) {
            throw new IllegalArgumentException("Solo se pueden cargar pedidos durante las fechas del festival.");
        }
        Empleado actor = empleadoService.traerPorUsername(username);
        if (!administrador && (actor == null || !actor.isActivo() || actor.getUnidadVenta() == null
                || !unidad.getId().equals(actor.getUnidadVenta().getId()))) {
            throw new org.springframework.security.access.AccessDeniedException("No tenés permiso para registrar pedidos en esta unidad.");
        }
        if (cierres.findByUnidadIdAndFecha(unidadId, hoy).isPresent())
            throw new IllegalArgumentException("La caja de esta unidad ya fue cerrada hoy. No se pueden registrar más ventas hasta mañana.");
        if (cantidad1 == null || cantidad1 < 1) throw new IllegalArgumentException("La cantidad del primer plato debe ser positiva.");
        Plato plato1 = validarPlato(unidad, plato1Id);
        Plato plato2 = null;
        if (plato2Id != null) {
            if (cantidad2 == null || cantidad2 < 1) throw new IllegalArgumentException("La cantidad del segundo plato debe ser positiva.");
            plato2 = validarPlato(unidad, plato2Id);
        } else if (cantidad2 != null && cantidad2 > 0) {
            throw new IllegalArgumentException("Elegí un segundo plato o dejá su cantidad en cero.");
        }

        Pedido pedido = new Pedido();
        pedido.setFechaTransaccion(java.time.LocalDateTime.now());
        pedido.setFestival(festival);
        pedido.setUnidadDeVenta(unidad);
        repository.save(pedido);
        guardarDetalle(pedido, plato1, cantidad1);
        if (plato2 != null) guardarDetalle(pedido, plato2, cantidad2);
        return pedido;
    }

    private Plato validarPlato(UnidadVenta unidad, Long platoId) {
        Plato plato = platos.findById(platoId).orElseThrow(() -> new IllegalArgumentException("El plato indicado no existe."));
        if (!unidad.getPlatos().stream().anyMatch(p -> p.getId().equals(platoId))) {
            throw new IllegalArgumentException("El plato seleccionado no pertenece a la unidad.");
        }
        return plato;
    }

    private void guardarDetalle(Pedido pedido, Plato plato, int cantidad) {
        if (plato.getPrecioVenta() == null || plato.getPrecioVenta() <= 0) throw new IllegalArgumentException("El plato no tiene un precio de venta válido.");
        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setPlato(plato);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(plato.getPrecioVenta());
        detalle.setCostoUnitario(plato.getCostoProduccion());
        detalles.save(detalle);
    }
}
