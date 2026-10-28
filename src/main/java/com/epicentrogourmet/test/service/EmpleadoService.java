package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository repository;
    private final ReporteService reportes;

    public EmpleadoService(EmpleadoRepository repository, ReporteService reportes) {
        this.repository = repository;
        this.reportes = reportes;
    }

    public Empleado agregar(Empleado objeto) {
        return repository.save(objeto);
    }

    public void actualizar(Empleado objeto) {
        repository.save(objeto);
    }

    // Baja lógica según pide el enunciado
    public void eliminarLogico(Long unidadId, Long idEmpleado) {
        Empleado emp = repository.findById(idEmpleado).orElseThrow(() -> new IllegalArgumentException("Empleado inexistente."));
        validarPropiedad(emp, unidadId);
        emp.setActivo(false);
        repository.save(emp);
    }

    public void reactivarEmpleado(Long unidadId, Empleado emp) {
        validarPropiedad(emp, unidadId);
        emp.setActivo(true);
        repository.save(emp);
    }

    private static void validarPropiedad(Empleado emp, Long unidadId) {
        if (emp.getUnidadVenta() == null || !unidadId.equals(emp.getUnidadVenta().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("El empleado no pertenece a la unidad autorizada.");
        }
    }

    public Empleado traer(Long idEmpleado) {
        return repository.findById(idEmpleado).orElse(null);
    }

    public Empleado traerPorUsername(String username) {
        return repository.findByUsuarioUsername(username).orElse(null);
    }

    public Empleado traerPorDni(Long dni) {
        return repository.findByDni(dni).orElse(null);
    }

    public List<Empleado> traer() {
        return repository.findAll();
    }

    public List<Empleado> traerActivosPorUnidad(Long unidadVentaId) {
        return repository.findByUnidadVentaIdAndActivoTrue(unidadVentaId);
    }

    public List<Empleado> traerInactivosPorUnidad(Long unidadVentaId) {
        return repository.findByUnidadVentaIdAndActivoFalse(unidadVentaId);
    }

    /*
     Liquidación de Haberes:
     Hasta 15 días: 50%
     De 16 a 30 días: 100%
     Más de 30 días: ((int)(diasDurFest / 15) + 1) * 50%
     Cocina: Sueldo Base + Plus por Categoría
     Cajero: Sueldo Base + (años de antigüedad * $5.000)
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public double calcularSueldoEnFestival(Empleado empleado, Festival festival) {
        return reportes.sueldo(empleado, festival).doubleValue();
    }

    public long calcularDiasFestival(Festival festival) {
        if (festival == null || festival.getFechaInicio() == null || festival.getFechaFin() == null) {
            return 0;
        }
        return Math.max(1, ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);
    }
}
