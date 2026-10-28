package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.service.CuentaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cuenta")
public class CuentaController {
    private final CuentaService cuentas;
    public CuentaController(CuentaService cuentas) { this.cuentas = cuentas; }

    @GetMapping("/ajustes")
    public String ajustes() { return "cuenta-ajustes"; }

    @PostMapping("/password")
    public String cambiar(@RequestParam String actual, @RequestParam String nueva,
                          @RequestParam String confirmacion, Authentication auth,
                          Model model, RedirectAttributes flash) {
        try {
            cuentas.cambiarPassword(auth.getName(), actual, nueva, confirmacion);
            flash.addFlashAttribute("mensajeExito", "Tu contraseña se actualizó. Usá la nueva contraseña en el próximo inicio de sesión.");
            return "redirect:/cuenta/ajustes";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "cuenta-ajustes";
        }
    }
}
