package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.AsignacionLaboral;
import com.epicentrogourmet.test.entity.Empleado;
import com.epicentrogourmet.test.entity.Festival;
import com.epicentrogourmet.test.entity.UnidadVenta;
import com.epicentrogourmet.test.repository.AsignacionLaboralRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AsignacionLaboralService {
    private final AsignacionLaboralRepository repository;
    private final ReporteService reportes;

    public AsignacionLaboralService(AsignacionLaboralRepository repository, ReporteService reportes) {
        this.repository = repository;
        this.reportes = reportes;
    }

    @Transactional(readOnly = true)
    public List<AsignacionLaboral> porEmpleado(Long empleadoId) {
        return repository.findByEmpleadoIdAndEmpleadoActivoTrue(empleadoId);
    }

    @Transactional(readOnly = true)
    public List<AsignacionLaboral> porUnidadYFestival(Long unidadId, Long festivalId) {
        return repository.findByUnidadIdAndFestivalId(unidadId, festivalId);
    }

    @Transactional(readOnly = true)
    public boolean estaAsignado(Long empleadoId, Long festivalId, Long unidadId) {
        return repository.existsByEmpleadoIdAndFestivalIdAndUnidadId(empleadoId, festivalId, unidadId);
    }

    @Transactional
    public AsignacionLaboral asignar(Empleado empleado, Festival festival, UnidadVenta unidad,
                                    LocalDate desde, LocalDate hasta) {
        if (empleado == null || festival == null || unidad == null || empleado.getId() == null
                || festival.getId() == null || unidad.getId() == null) {
            throw new IllegalArgumentException("Empleado, festival y unidad deben existir.");
        }
        if (!empleado.isActivo()) throw new IllegalArgumentException("No se puede asignar un empleado inactivo.");
        if (empleado.getUnidadVenta() == null || !empleado.getUnidadVenta().getId().equals(unidad.getId())) {
            throw new IllegalArgumentException("El empleado debe pertenecer a esa unidad de venta.");
        }
        if (!unidad.getFestivales().stream().anyMatch(f -> f.getId().equals(festival.getId()))) {
            throw new IllegalArgumentException("La unidad no participa en ese festival.");
        }
        LocalDate inicio = desde == null ? festival.getFechaInicio() : desde;
        if (empleado.getFechaIngreso() != null && inicio.isBefore(empleado.getFechaIngreso())) inicio = empleado.getFechaIngreso();
        LocalDate fin = hasta == null ? festival.getFechaFin() : hasta;
        if (inicio == null || fin == null || inicio.isBefore(festival.getFechaInicio())
                || fin.isAfter(festival.getFechaFin()) || fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El período debe estar dentro de las fechas del festival.");
        }
        var existente = repository.findByEmpleadoIdAndFestivalIdAndUnidadIdAndFechaInicio(
                empleado.getId(), festival.getId(), unidad.getId(), inicio);
        if (existente.isPresent()) return existente.get();
        AsignacionLaboral asignacion = new AsignacionLaboral();
        asignacion.setEmpleado(empleado);
        asignacion.setFestival(festival);
        asignacion.setUnidad(unidad);
        asignacion.setFechaInicio(inicio);
        asignacion.setFechaFin(fin);
        asignacion.setSueldoLiquidado(reportes.sueldo(empleado, festival, inicio, fin));
        return repository.save(asignacion);
    }
}
