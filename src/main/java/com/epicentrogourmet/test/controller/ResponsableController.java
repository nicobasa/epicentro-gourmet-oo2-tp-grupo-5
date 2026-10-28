package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.service.*;
import com.epicentrogourmet.test.dto.AltaEmpleadoForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/responsable")
public class ResponsableController {
    private final EmpleadoService empleados;
    private final EmpleadoGestionService gestion;
    private final FestivalService festivales;
    private final UnidadVentaService unidades;
    private final PlatoService platos;
    private final AsignacionLaboralService asignaciones;
    private final ReporteService reportes;

    public ResponsableController(EmpleadoService empleados, EmpleadoGestionService gestion, FestivalService festivales,
                                 UnidadVentaService unidades, PlatoService platos,
                                 AsignacionLaboralService asignaciones, ReporteService reportes) {
        this.empleados = empleados;
        this.gestion = gestion;
        this.festivales = festivales;
        this.unidades = unidades;
        this.platos = platos;
        this.asignaciones = asignaciones;
        this.reportes = reportes;
    }

    @GetMapping("/panel")
    public String panel(@RequestParam(required = false) Long unidadId,
                        @RequestParam(required = false) Long festivalId,
                        @RequestParam(required = false) String nombre,
                        @RequestParam(required = false) String dni,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false) LocalDate desde,
                        @RequestParam(required = false) LocalDate hasta,
                        Authentication auth, Model model) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        Long dniBuscado = null;
        if (dni != null && !dni.isBlank()) {
            if (dni.matches("[0-9]{7,8}")) dniBuscado = Long.valueOf(dni);
            else model.addAttribute("error", "El filtro DNI debe tener entre 7 y 8 dígitos, sin puntos ni letras.");
        }
        final Long filtroDniNumero = dniBuscado;
        List<Festival> eventos = unidad.getFestivales();
        Festival festival = festivalId == null ? eventos.stream().findFirst().orElse(null)
                : eventos.stream().filter(f -> f.getId().equals(festivalId)).findFirst()
                .orElseThrow(() -> new AccessDeniedException("La unidad no participa de ese festival."));
        List<Empleado> lista = empleados.traerActivosPorUnidad(unidad.getId()).stream()
                .filter(e -> filtroDniNumero == null || filtroDniNumero.equals(e.getDni()))
                .filter(e -> nombre == null || nombre.isBlank() || (e.getNombre() + " " + e.getApellido()).toLowerCase().contains(nombre.toLowerCase()))
                .filter(e -> tipo == null || tipo.isBlank() || (e instanceof Cajero ? "Cajero" : e instanceof Cocinero ? "Cocinero" : "Empleado").equalsIgnoreCase(tipo))
                .toList();
        Map<Long, java.math.BigDecimal> sueldos = new HashMap<>();
        if (festival != null) {
            for (var a : asignaciones.porUnidadYFestival(unidad.getId(), festival.getId()))
                sueldos.merge(a.getEmpleado().getId(), reportes.sueldo(a), java.math.BigDecimal::add);
        }
        model.addAttribute("responsable", empleados.traerPorUsername(auth.getName()));
        model.addAttribute("unidad", unidad);
        model.addAttribute("unidades", esAdmin(auth) ? unidades.traer() : List.of(unidad));
        model.addAttribute("empleados", lista);
        model.addAttribute("empleadosInactivos", empleados.traerInactivosPorUnidad(unidad.getId()));
        if (!model.containsAttribute("altaForm")) {
            AltaEmpleadoForm form = new AltaEmpleadoForm();
            form.setUnidadId(unidad.getId());
            model.addAttribute("altaForm", form);
        }
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("festivales", eventos);
        model.addAttribute("festivalSeleccionado", festival);
        model.addAttribute("sueldos", sueldos);
        model.addAttribute("platos", unidad.getPlatos());
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("filtroDni", dni);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("asignacionesPorFestival", festival == null ? List.of() : asignaciones.porUnidadYFestival(unidad.getId(), festival.getId()));
        model.addAttribute("recaudacion", festival == null ? null : reportes.recaudacion(festival.getId(), unidad.getId()));
        model.addAttribute("reporteVenta", festival == null ? null : reportes.reporteVenta(festival.getId()));
        model.addAttribute("canon", festival == null ? null : reportes.canon(unidad, festival));
        try {
            model.addAttribute("rentabilidad", festival == null ? null : reportes.rentabilidad(festival.getId(), unidad.getId(), desde, hasta));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("rentabilidad", null);
        }
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("platoEstrella", festival == null ? null : reportes.platoEstrella(festival.getId(), unidad.getId()));
        return "responsable-panel";
    }

    @PostMapping("/empleados/alta")
    public String alta(@Valid @ModelAttribute("altaForm") AltaEmpleadoForm form, BindingResult binding,
                       Authentication auth, RedirectAttributes flash, Model model) {
        UnidadVenta unidad = unidadAutorizada(auth, form.getUnidadId());
        if (binding.hasErrors()) {
            String errores = binding.getFieldErrors().stream().map(e -> e.isBindingFailure()
                    ? "Revisá el valor de " + e.getField() + "." : e.getDefaultMessage()).distinct()
                    .collect(java.util.stream.Collectors.joining(" "));
            model.addAttribute("error", errores);
            return panel(unidad.getId(), null, null, null, null, null, null, auth, model);
        }
        try {
            gestion.alta(unidad.getId(), form.getTipoEmpleado(), form.getNombre(), form.getApellido(), Long.valueOf(form.getDni()),
                    form.getFechaNacimiento(), form.getEmailUsuario(), form.getTurno(), form.getEspecialidadCulinaria(),
                    form.getCategoriaCocina(), form.getFestivalIds());
            flash.addFlashAttribute("mensajeExito", "Empleado registrado. Las credenciales se enviaron por correo.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return panel(unidad.getId(), null, null, null, null, null, null, auth, model);
        }
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    @PostMapping("/empleados/modificar/{id}")
    public String modificar(@PathVariable Long id, @RequestParam Long unidadId,
                            @RequestParam String nombre, @RequestParam String apellido,
                            @RequestParam LocalDate fechaNacimiento, @RequestParam String emailUsuario,
                            @RequestParam(required = false) String turno,
                            @RequestParam(required = false) String especialidadCulinaria,
                            @RequestParam(required = false) CategoriaCocina categoriaCocina,
                            Authentication auth, RedirectAttributes flash) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        try {
            gestion.modificar(unidad.getId(), id, nombre, apellido, fechaNacimiento, emailUsuario, turno, especialidadCulinaria, categoriaCocina);
            flash.addFlashAttribute("mensajeExito", "Empleado actualizado.");
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    @PostMapping("/empleados/baja/{id}")
    public String baja(@PathVariable Long id, @RequestParam Long unidadId,
                       Authentication auth, RedirectAttributes flash) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        try {
            gestion.bajaLogica(unidad.getId(), id);
            flash.addFlashAttribute("mensajeExito", "Empleado dado de baja lógica.");
        } catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    @PostMapping("/empleados/reactivar/{id}")
    public String reactivar(@PathVariable Long id, @RequestParam Long unidadId,
                            Authentication auth, RedirectAttributes flash) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        try {
            gestion.reactivar(unidad.getId(), id);
            flash.addFlashAttribute("mensajeExito", "Empleado reactivado. Conserva su cuenta, contraseña, fecha de ingreso e historial.");
        } catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    @PostMapping("/empleados/{id}/asignar-festival")
    public String asignarFestival(@PathVariable Long id, @RequestParam Long unidadId,
                                  @RequestParam Long festivalId, Authentication auth, RedirectAttributes flash) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        Empleado empleado = Optional.ofNullable(empleados.traer(id)).orElseThrow(() -> new IllegalArgumentException("Empleado inexistente."));
        try {
            Festival festival = Optional.ofNullable(festivales.traer(festivalId)).orElseThrow(() -> new IllegalArgumentException("Festival inexistente."));
            asignaciones.asignar(empleado, festival, unidad, festival.getFechaInicio(), festival.getFechaFin());
            flash.addFlashAttribute("mensajeExito", "Empleado asignado al festival.");
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    @PostMapping("/platos/alta")
    public String altaPlato(@RequestParam Long unidadId, @RequestParam String nombre,
                            @RequestParam Double precio, @RequestParam Double costo,
                            Authentication auth, RedirectAttributes flash) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        try { platos.agregarAUnidad(unidad.getId(), nombre, precio, costo); flash.addFlashAttribute("mensajeExito", "Plato agregado al menú."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    @PostMapping("/platos/modificar/{id}")
    public String modificarPlato(@PathVariable Long id, @RequestParam Long unidadId,
                                 @RequestParam String nombre, @RequestParam Double precio,
                                 @RequestParam Double costo, Authentication auth, RedirectAttributes flash) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        try { platos.modificarDeUnidad(unidad.getId(), id, nombre, precio, costo); flash.addFlashAttribute("mensajeExito", "Plato actualizado."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    @PostMapping("/platos/baja/{id}")
    public String bajaPlato(@PathVariable Long id, @RequestParam Long unidadId,
                            Authentication auth, RedirectAttributes flash) {
        UnidadVenta unidad = unidadAutorizada(auth, unidadId);
        platos.bajaDeUnidad(unidad.getId(), id);
        flash.addFlashAttribute("mensajeExito", "Plato quitado del menú. Los pedidos históricos se conservaron.");
        return "redirect:/responsable/panel?unidadId=" + unidad.getId();
    }

    private UnidadVenta unidadAutorizada(Authentication auth, Long unidadId) {
        if (esAdmin(auth)) {
            if (unidadId == null) return unidades.traer().stream().findFirst().orElseThrow(() -> new IllegalArgumentException("No hay unidades registradas."));
            return Optional.ofNullable(unidades.traer(unidadId)).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        }
        Empleado responsable = empleados.traerPorUsername(auth.getName());
        if (responsable == null || !responsable.isActivo() || responsable.getUnidadVenta() == null) throw new AccessDeniedException("La cuenta no tiene una unidad asignada.");
        if (unidadId != null && !unidadId.equals(responsable.getUnidadVenta().getId())) throw new AccessDeniedException("No podés acceder a otra unidad.");
        return responsable.getUnidadVenta();
    }

    private boolean esAdmin(Authentication auth) { return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())); }
}
