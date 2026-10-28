package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.service.AsignacionLaboralService;
import com.epicentrogourmet.test.service.EmpleadoService;
import com.epicentrogourmet.test.service.ReporteService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {
    private final EmpleadoService empleados;
    private final AsignacionLaboralService asignaciones;
    private final ReporteService reportes;

    public EmpleadoController(EmpleadoService empleados, AsignacionLaboralService asignaciones, ReporteService reportes) {
        this.empleados = empleados;
        this.asignaciones = asignaciones;
        this.reportes = reportes;
    }

    @GetMapping("/panel")
    public String panel(Authentication auth, Model model) {
        Empleado empleado = empleados.traerPorUsername(auth.getName());
        if (empleado == null || !empleado.isActivo()) throw new AccessDeniedException("No hay un empleado activo asociado a esta cuenta.");
        var historial = asignaciones.porEmpleado(empleado.getId());
        Map<Long, java.math.BigDecimal> sueldos = new HashMap<>();
        for (var asignacion : historial) sueldos.put(asignacion.getId(), reportes.sueldo(asignacion));
        var ahora = LocalDate.now();
        var vigente = historial.stream().filter(a -> !ahora.isBefore(a.getFechaInicio()) && !ahora.isAfter(a.getFechaFin())).findFirst().orElse(null);
        String tipo = empleado instanceof Cajero c ? "Cajero — turno " + c.getTurno()
                : empleado instanceof Cocinero c ? "Cocina — " + c.getCategoria().getDescripcion() : empleado.getPuestoDescripcion();
        model.addAttribute("empleado", empleado);
        model.addAttribute("tipoPuesto", tipo);
        model.addAttribute("esCajero", empleado instanceof Cajero);
        model.addAttribute("historial", historial);
        model.addAttribute("sueldos", sueldos);
        model.addAttribute("asignacionActual", vigente);
        return "empleado-panel";
    }
}
