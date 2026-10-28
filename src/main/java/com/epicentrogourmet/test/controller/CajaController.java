package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.entity.Cajero;
import com.epicentrogourmet.test.entity.Empleado;
import com.epicentrogourmet.test.service.CierreCajaService;
import com.epicentrogourmet.test.service.EmpleadoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/caja")
public class CajaController {
    private final EmpleadoService empleados;
    private final CierreCajaService cierres;
    public CajaController(EmpleadoService empleados, CierreCajaService cierres) { this.empleados = empleados; this.cierres = cierres; }

    @GetMapping
    public String panel(Authentication auth, Model model) {
        Cajero cajero = cajeroActual(auth);
        model.addAttribute("cajero", cajero);
        return "caja-panel";
    }

    @PostMapping("/cerrar")
    public String cerrar(Authentication auth, RedirectAttributes flash) {
        try {
            var cierre = cierres.cerrarHoy(cajeroActual(auth));
            flash.addFlashAttribute("mensajeExito", "Caja cerrada por $ " + cierre.getTotal().toPlainString());
        } catch (IllegalStateException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/caja";
    }

    private Cajero cajeroActual(Authentication auth) {
        Empleado empleado = empleados.traerPorUsername(auth.getName());
        if (!(empleado instanceof Cajero cajero) || !cajero.isActivo()) throw new AccessDeniedException("El cierre de caja está disponible únicamente para Cajeros activos.");
        return cajero;
    }
}
