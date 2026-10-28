package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.dto.FestivalForm;
import com.epicentrogourmet.test.service.FestivalService;
import com.epicentrogourmet.test.service.ReporteService;
import com.epicentrogourmet.test.service.UnidadVentaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final FestivalService festivales;
    private final UnidadVentaService unidades;
    private final ReporteService reportes;
    public AdminController(FestivalService festivales, UnidadVentaService unidades, ReporteService reportes) {
        this.festivales = festivales; this.unidades = unidades; this.reportes = reportes;
    }
    @GetMapping("/panel")
    public String panel(@RequestParam(required = false) Long festivalId,
                        @RequestParam(defaultValue = "3") int cantidad, Model model) {
        var todos = festivales.traer();
        var seleccionado = festivalId == null ? todos.stream().findFirst().orElse(null) : festivales.traer(festivalId);
        if (cantidad < 1 || cantidad > 100) cantidad = 3;
        model.addAttribute("festivales", todos); model.addAttribute("todasLasUnidades", unidades.traer());
        model.addAttribute("festivalSeleccionado", seleccionado); model.addAttribute("cantidad", cantidad);
        model.addAttribute("costosPredeterminados", com.epicentrogourmet.test.entity.Costo.valoresPredeterminados());
        if (seleccionado != null) {
            model.addAttribute("unidades", seleccionado.getUnidadesDeVenta());
            model.addAttribute("ranking", reportes.ranking(seleccionado.getId()));
            model.addAttribute("mayoresCanon", reportes.mayoresCanon(seleccionado.getId(), cantidad));
        }
        return "admin-panel";
    }
    @PostMapping("/festivales/alta")
    public String crearFestival(@Valid @ModelAttribute FestivalForm datos, BindingResult errores, RedirectAttributes flash) {
        if (errores.hasErrors()) { flash.addFlashAttribute("error", mensaje(errores)); return "redirect:/admin/panel"; }
        try {
            var nuevo = festivales.crear(datos);
            flash.addFlashAttribute("mensajeExito", "Festival creado correctamente.");
            return "redirect:/admin/panel?festivalId=" + nuevo.getId();
        } catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/panel";
    }
    @PostMapping("/festivales/modificar/{id}")
    public String modificarFestival(@PathVariable Long id, @Valid @ModelAttribute FestivalForm datos,
                                    BindingResult errores, RedirectAttributes flash) {
        if (errores.hasErrors()) flash.addFlashAttribute("error", mensaje(errores));
        else try {
            festivales.modificar(id, datos);
            flash.addFlashAttribute("mensajeExito", "Festival y costos actualizados. Se conservan los valores históricos ya registrados.");
        } catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/panel?festivalId=" + id;
    }
    @PostMapping("/festivales/eliminar/{id}")
    public String eliminarFestival(@PathVariable Long id, RedirectAttributes flash) {
        try { festivales.eliminarSinHistorial(id); flash.addFlashAttribute("mensajeExito", "Festival eliminado correctamente."); }
        catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/panel";
    }
    @PostMapping("/festivales/{id}/vincular-unidad")
    public String vincularUnidad(@PathVariable Long id, @RequestParam Long unidadId, RedirectAttributes flash) {
        try { festivales.vincularUnidad(id, unidadId); flash.addFlashAttribute("mensajeExito", "Unidad habilitada en el festival."); }
        catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/panel?festivalId=" + id;
    }
    @PostMapping("/festivales/{id}/quitar-unidad/{unidadId}")
    public String quitarUnidad(@PathVariable Long id, @PathVariable Long unidadId, RedirectAttributes flash) {
        try { festivales.quitarUnidad(id, unidadId); flash.addFlashAttribute("mensajeExito", "Unidad desvinculada del festival."); }
        catch (IllegalArgumentException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/panel?festivalId=" + id;
    }
    private static String mensaje(BindingResult errores) {
        return errores.getAllErrors().stream().map(e -> e instanceof org.springframework.validation.FieldError f && f.isBindingFailure()
                ? "Ingresá un valor numérico o una fecha válida en " + f.getField() + "."
                : e.getDefaultMessage()).distinct().collect(java.util.stream.Collectors.joining(" "));
    }
}
