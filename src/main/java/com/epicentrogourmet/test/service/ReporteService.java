package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.dto.PlatoDTO;
import com.epicentrogourmet.test.dto.ReporteMayoresCanonDTO;
import com.epicentrogourmet.test.dto.ReporteVentaDTO;
import com.epicentrogourmet.test.dto.ResumenUnidadDTO;
import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.AsignacionLaboralRepository;
import com.epicentrogourmet.test.repository.DetallePedidoRepository;
import com.epicentrogourmet.test.repository.FestivalRepository;
import com.epicentrogourmet.test.repository.PedidoRepository;
import com.epicentrogourmet.test.repository.UnidadVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReporteService {
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final UnidadVentaRepository unidades;
    private final AsignacionLaboralRepository asignaciones;
    private final FestivalRepository festivales;
    private final com.epicentrogourmet.test.repository.CanonUnidadFestivalRepository canones;

    public ReporteService(PedidoRepository pedidos, DetallePedidoRepository detalles,
                          UnidadVentaRepository unidades, AsignacionLaboralRepository asignaciones,
                          FestivalRepository festivales, com.epicentrogourmet.test.repository.CanonUnidadFestivalRepository canones) {
        this.pedidos = pedidos;
        this.detalles = detalles;
        this.unidades = unidades;
        this.asignaciones = asignaciones;
        this.festivales = festivales;
        this.canones = canones;
    }

    @Transactional(readOnly = true)
    public BigDecimal canon(UnidadVenta unidad, Festival festival) {
        if (unidad != null && festival != null && unidad.getId() != null && festival.getId() != null) {
            var historico = canones.findByFestivalIdAndUnidadId(festival.getId(), unidad.getId());
            if (historico.isPresent()) return historico.get().getImporte();
        }
        return calcularCanon(unidad, festival);
    }

    @Transactional
    public void conservarCanon(UnidadVenta unidad, Festival festival) {
        if (canones.findByFestivalIdAndUnidadId(festival.getId(), unidad.getId()).isEmpty()) {
            var historico = new CanonUnidadFestival();
            historico.setFestival(festival); historico.setUnidad(unidad);
            historico.setImporte(calcularCanon(unidad, festival));
            canones.save(historico);
        }
    }

    private BigDecimal calcularCanon(UnidadVenta unidad, Festival festival) {
        unidad = (UnidadVenta) org.hibernate.Hibernate.unproxy(unidad);
        if (unidad == null || festival == null || festival.getCosto() == null || unidad.getSuperficie() == null) {
            throw new IllegalArgumentException("La unidad y los costos del festival son obligatorios.");
        }
        Costo costo = festival.getCosto();
        BigDecimal area = BigDecimal.valueOf(unidad.getSuperficie());
        BigDecimal resultado = area.multiply(requerido(costo.getCostoPorSuperficie(), "costo por superficie"));
        if (unidad instanceof FoodTruck truck) {
            if (truck.isRequiereElectricidad()) resultado = resultado.add(requerido(costo.getPlusElectricidad(), "plus eléctrico"));
        } else if (unidad instanceof PuestoDesarmable puesto) {
            if (puesto.getTiempoMontaje() == null) throw new IllegalArgumentException("Falta el tiempo de montaje.");
            resultado = resultado.subtract(BigDecimal.valueOf(puesto.getTiempoMontaje())
                    .multiply(requerido(costo.getCostoPorMontaje(), "costo por montaje")));
        }
        return resultado.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal sueldo(Empleado empleado, Festival festival) {
        return sueldo(empleado, festival, null, null);
    }

    @Transactional(readOnly = true)
    public BigDecimal sueldo(Empleado empleado, Festival festival, LocalDate asignadoDesde, LocalDate asignadoHasta) {
        empleado = (Empleado) org.hibernate.Hibernate.unproxy(empleado);
        if (empleado == null || festival == null || festival.getFechaInicio() == null || festival.getFechaFin() == null
                || empleado.getFechaIngreso() == null || empleado.getFechaIngreso().isAfter(festival.getFechaFin())) {
            return BigDecimal.ZERO.setScale(2);
        }
        LocalDate inicioTrabajo = festival.getFechaInicio().isAfter(empleado.getFechaIngreso()) ? festival.getFechaInicio() : empleado.getFechaIngreso();
        if (asignadoDesde != null && asignadoDesde.isAfter(inicioTrabajo)) inicioTrabajo = asignadoDesde;
        LocalDate finTrabajo = asignadoHasta != null && asignadoHasta.isBefore(festival.getFechaFin()) ? asignadoHasta : festival.getFechaFin();
        if (inicioTrabajo.isAfter(finTrabajo)) return BigDecimal.ZERO.setScale(2);
        Costo costo = festival.getCosto();
        BigDecimal base = costo != null && costo.getSueldoBase() != null ? costo.getSueldoBase()
                : empleado.getSueldoBase() == null ? null : BigDecimal.valueOf(empleado.getSueldoBase());
        if (base == null) throw new IllegalArgumentException("Falta configurar el sueldo base.");
        BigDecimal baseConAdicional = base;
        if (empleado instanceof Cocinero cocinero) {
            BigDecimal plus = plusCocina(cocinero, costo);
            baseConAdicional = baseConAdicional.add(plus);
        } else if (empleado instanceof Cajero) {
            BigDecimal porAnio = costo == null ? null : costo.getMontoAnioAntiguedad();
            if (porAnio == null) throw new IllegalArgumentException("Falta configurar el monto por antigüedad.");
            int anios = Math.max(0, java.time.Period.between(empleado.getFechaIngreso(), inicioTrabajo).getYears());
            baseConAdicional = baseConAdicional.add(porAnio.multiply(BigDecimal.valueOf(anios)));
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicioTrabajo, finTrabajo) + 1;
        if (dias <= 0) throw new IllegalArgumentException("El período del festival es inválido.");
        BigDecimal factor = dias <= 15 ? new BigDecimal("0.5") : dias <= 30 ? BigDecimal.ONE
                : BigDecimal.valueOf((dias / 15 + 1) * 0.5);
        return baseConAdicional.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal plusCocina(Cocinero cocinero, Costo costo) {
        if (costo == null || cocinero.getCategoria() == null) throw new IllegalArgumentException("Falta categoría o costos de cocina.");
        return switch (cocinero.getCategoria()) {
            case COCINERO -> requerido(costo.getPlusCocinero(), "plus cocinero");
            case COCINERO_AYUDANTE -> requerido(costo.getPlusCocineroAyudante(), "plus ayudante");
            case LAVAPLATOS -> requerido(costo.getPlusLavaplatos(), "plus lavaplatos");
        };
    }

    public BigDecimal sueldo(AsignacionLaboral asignacion) {
        return asignacion.getSueldoLiquidado() != null ? asignacion.getSueldoLiquidado()
                : sueldo(asignacion.getEmpleado(), asignacion.getFestival(), asignacion.getFechaInicio(), asignacion.getFechaFin());
    }

    @Transactional(readOnly = true)
    public List<ResumenUnidadDTO> ranking(Long festivalId) {
        return unidades.findDistinctByFestivalesId(festivalId).stream()
                .map(unidad -> new ResumenUnidadDTO(unidad.getId(), unidad.getNombreComercial(), unidad.getCodigo(),
                        recaudacion(festivalId, unidad.getId())))
                .sorted(Comparator.comparing(ResumenUnidadDTO::recaudacion).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> mayoresCanon(Long festivalId, int cantidad) {
        if (cantidad < 1 || cantidad > 100) throw new IllegalArgumentException("La cantidad debe estar entre 1 y 100.");
        Festival festival = requerirFestival(festivalId);
        return unidades.findDistinctByFestivalesId(festivalId).stream()
                .map(unidad -> new ReporteMayoresCanonDTO(unidad.getNombreComercial(), unidad.getCodigo(), tipo(unidad), canon(unidad, festival)))
                .sorted(Comparator.comparing(ReporteMayoresCanonDTO::canon).reversed())
                .limit(cantidad).toList();
    }

    @Transactional(readOnly = true)
    public ReporteVentaDTO reporteVenta(Long festivalId) {
        Festival festival = requerirFestival(festivalId);
        BigDecimal total = BigDecimal.ZERO;
        for (Pedido pedido : pedidos.findByFestivalId(festivalId)) total = total.add(importePedido(pedido));
        return new ReporteVentaDTO(festival.getId(), festival.getNombre(), total.setScale(2, RoundingMode.HALF_UP));
    }

    @Transactional(readOnly = true)
    public BigDecimal rentabilidad(Long festivalId, Long unidadId, LocalDate desde, LocalDate hasta) {
        Festival festival = requerirFestival(festivalId);
        UnidadVenta unidad = unidades.findById(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        if (!unidad.getFestivales().stream().anyMatch(f -> f.getId().equals(festivalId))) {
            throw new IllegalArgumentException("La unidad no participa en ese festival.");
        }
        LocalDate inicio = desde == null ? festival.getFechaInicio() : desde;
        LocalDate fin = hasta == null ? festival.getFechaFin() : hasta;
        if (inicio.isBefore(festival.getFechaInicio()) || fin.isAfter(festival.getFechaFin()) || fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El rango debe estar dentro de las fechas del festival.");
        }
        LocalDateTime start = inicio.atStartOfDay();
        LocalDateTime end = fin.plusDays(1).atStartOfDay().minusNanos(1);
        BigDecimal neto = BigDecimal.ZERO;
        for (Pedido pedido : pedidos.findByUnidadDeVentaIdAndFechaTransaccionBetween(unidadId, start, end)) {
            if (pedido.getFestival() != null && pedido.getFestival().getId().equals(festivalId)) {
                neto = neto.add(importePedido(pedido)).subtract(costoProduccion(pedido));
            }
        }
        long diasFestival = java.time.temporal.ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1;
        long diasPeriodo = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
        BigDecimal proporcion = BigDecimal.valueOf(diasPeriodo).divide(BigDecimal.valueOf(diasFestival), 8, RoundingMode.HALF_UP);
        BigDecimal sueldos = asignaciones.findByUnidadIdAndFestivalId(unidadId, festivalId).stream()
                .map(a -> sueldoEnPeriodo(a, inicio, fin)).reduce(BigDecimal.ZERO, BigDecimal::add);
        neto = neto.subtract(sueldos).subtract(canon(unidad, festival).multiply(proporcion));
        return neto.setScale(2, RoundingMode.HALF_UP);
    }

    /** Mayor cantidad de pedidos distintos; en un empate gana el menor ID de plato. */
    @Transactional(readOnly = true)
    public PlatoDTO platoEstrella(Long festivalId, Long unidadId) {
        requerirFestival(festivalId);
        var pedidosPorPlato = new HashMap<Long, Set<Long>>();
        var nombres = new HashMap<Long, String>();
        for (Pedido pedido : pedidos.findByFestivalIdAndUnidadDeVentaId(festivalId, unidadId)) {
            for (DetallePedido detalle : detalles.findByPedidoId(pedido.getId())) {
                if (detalle.getPlato() != null && detalle.getPlato().getId() != null) {
                    long id = detalle.getPlato().getId();
                    pedidosPorPlato.computeIfAbsent(id, ignored -> new HashSet<>()).add(pedido.getId());
                    nombres.put(id, detalle.getPlato().getNombre());
                }
            }
        }
        return pedidosPorPlato.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Long, Set<Long>>>comparingInt(e -> e.getValue().size())
                        .reversed().thenComparing(Map.Entry::getKey))
                .findFirst()
                .map(e -> new PlatoDTO(e.getKey(), nombres.get(e.getKey()), (long) e.getValue().size())).orElse(null);
    }

    @Transactional(readOnly = true)
    public BigDecimal recaudacion(Long festivalId, Long unidadId) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pedido pedido : pedidos.findByFestivalIdAndUnidadDeVentaId(festivalId, unidadId)) total = total.add(importePedido(pedido));
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal importePedido(Pedido pedido) {
        return detalles.findByPedidoId(pedido.getId()).stream()
                .filter(d -> d.getCantidad() != null && d.getPrecioUnitario() != null)
                .map(d -> BigDecimal.valueOf(d.getPrecioUnitario()).multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal costoProduccion(Pedido pedido) {
        return detalles.findByPedidoId(pedido.getId()).stream()
                .filter(d -> d.getCantidad() != null)
                .map(d -> BigDecimal.valueOf(d.getCostoUnitario() != null ? d.getCostoUnitario()
                        : d.getPlato() != null && d.getPlato().getCostoProduccion() != null ? d.getPlato().getCostoProduccion() : 0)
                        .multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sueldoEnPeriodo(AsignacionLaboral a, LocalDate desde, LocalDate hasta) {
        LocalDate inicio = a.getFechaInicio().isAfter(desde) ? a.getFechaInicio() : desde;
        LocalDate fin = a.getFechaFin().isBefore(hasta) ? a.getFechaFin() : hasta;
        if (fin.isBefore(inicio)) return BigDecimal.ZERO;
        long diasAsignacion = java.time.temporal.ChronoUnit.DAYS.between(a.getFechaInicio(), a.getFechaFin()) + 1;
        long diasPeriodo = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
        return sueldo(a).multiply(BigDecimal.valueOf(diasPeriodo))
                .divide(BigDecimal.valueOf(diasAsignacion), 8, RoundingMode.HALF_UP);
    }

    private Festival requerirFestival(Long id) {
        if (id == null) throw new IllegalArgumentException("Festival obligatorio.");
        return festivales.findById(id).orElseThrow(() -> new IllegalArgumentException("Festival inexistente."));
    }

    private static BigDecimal requerido(BigDecimal value, String nombre) {
        if (value == null) throw new IllegalArgumentException("Falta configurar " + nombre + ".");
        return value;
    }

    private static String tipo(UnidadVenta unidad) {
        if (unidad instanceof FoodTruck) return "Food Truck";
        if (unidad instanceof PuestoDesarmable) return "Puesto Desarmable";
        return "Unidad de Venta";
    }
}
