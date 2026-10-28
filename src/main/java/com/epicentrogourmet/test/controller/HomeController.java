package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.service.FestivalService;
import com.epicentrogourmet.test.service.EmpleadoService;
import com.epicentrogourmet.test.service.RegistroResponsableService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class HomeController {
    private final FestivalService festivalService;
    private final RegistroResponsableService registroResponsableService;
    private final EmpleadoService empleadoService;

    public HomeController(FestivalService festivalService, RegistroResponsableService registroResponsableService,
                          EmpleadoService empleadoService) {
        this.festivalService = festivalService;
        this.registroResponsableService = registroResponsableService;
        this.empleadoService = empleadoService;
    }

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        model.addAttribute("festivales", festivalService.traer());
        String puestoStaff = "Empleado";
        if (auth != null && auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_EMPLEADO".equals(a.getAuthority()) || "ROLE_CAJERO".equals(a.getAuthority()))) {
            var empleado = empleadoService.traerPorUsername(auth.getName());
            if (empleado != null) puestoStaff = empleado.getPuestoDescripcion();
            else if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_CAJERO".equals(a.getAuthority())))
                puestoStaff = "Cajero";
        }
        model.addAttribute("puestoStaff", puestoStaff);
        return "home";
    }

    @GetMapping("/registro-unidad")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("festivales", festivalService.traer());
        return "registro-unidad";
    }

    @PostMapping("/registro-unidad")
    public String registrarUnidadConResponsable(
            @RequestParam Long festivalId,
            @RequestParam String tipoUnidad,
            @RequestParam String nombreComercial,
            @RequestParam Double superficie,
            @RequestParam String codigo,
            @RequestParam(required = false) String patente,
            @RequestParam(defaultValue = "false") boolean requiereElectricidad,
            @RequestParam(required = false) Integer cantidadCarpas,
            @RequestParam(required = false) Integer tiempoMontaje,
            @RequestParam String nombreResponsable,
            @RequestParam String apellidoResponsable,
            @RequestParam String dniResponsable,
            @RequestParam LocalDate fechaNacimientoResponsable,
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            if (dniResponsable == null || !dniResponsable.matches("[0-9]{7,8}"))
                throw new IllegalArgumentException("El DNI del Responsable debe tener entre 7 y 8 dígitos, sin puntos ni letras.");
            registroResponsableService.registrar(festivalId, tipoUnidad, nombreComercial, superficie, codigo,
                    patente, requiereElectricidad, cantidadCarpas, tiempoMontaje, nombreResponsable,
                    apellidoResponsable, Long.valueOf(dniResponsable), fechaNacimientoResponsable, username, password);
            redirectAttributes.addFlashAttribute("mensajeExito", "Unidad y cuenta Responsable registradas. Ya podés iniciar sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("festivales", festivalService.traer());
            return "registro-unidad";
        }
    }
}
