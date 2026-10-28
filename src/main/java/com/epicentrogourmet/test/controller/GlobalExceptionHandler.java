package com.epicentrogourmet.test.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.ServletRequestBindingException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ServletRequestBindingException.class})
    public String campoInvalido(Exception ex, HttpServletRequest request, RedirectAttributes flash, Model model,
                                jakarta.servlet.http.HttpServletResponse response) {
        String campo = ex instanceof MethodArgumentTypeMismatchException mismatch ? mismatch.getName() : "";
        String mensaje = campo.toLowerCase().contains("dni")
                ? "El DNI debe tener entre 7 y 8 dígitos, sin puntos ni letras."
                : campo.toLowerCase().contains("fecha") || "desde".equals(campo) || "hasta".equals(campo)
                ? "Ingresá una fecha válida en los campos del formulario."
                : "Revisá los campos obligatorios y los valores numéricos del formulario.";
        return volverAlFormulario(request, flash, model, response, mensaje, 400);
    }

    private String volverAlFormulario(HttpServletRequest request, RedirectAttributes flash, Model model,
                                       jakarta.servlet.http.HttpServletResponse response, String mensaje, int status) {
        String path = request.getRequestURI();
        String destino = path.startsWith("/admin/") ? "/admin/panel"
                : path.startsWith("/responsable/") ? "/responsable/panel"
                : path.startsWith("/pedidos/") ? "/pedidos/nuevo"
                : path.equals("/registro-unidad") ? "/registro-unidad"
                : path.startsWith("/cuenta/") ? "/cuenta/ajustes" : null;
        if (destino != null) {
            String unidad = request.getParameter("unidadId");
            if (destino.startsWith("/responsable/") && unidad != null && unidad.matches("[0-9]{1,18}"))
                destino += "?unidadId=" + unidad;
            flash.addFlashAttribute("error", mensaje);
            return "redirect:" + destino;
        }
        response.setStatus(status);
        model.addAttribute("status", status);
        model.addAttribute("mensaje", mensaje);
        return "error";
    }
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("mensaje", "No tenés permiso para acceder a este recurso.");
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String invalidRequest(IllegalArgumentException ex, Model model, HttpServletRequest request,
                                 RedirectAttributes flash, jakarta.servlet.http.HttpServletResponse response) {
        return volverAlFormulario(request, flash, model, response, ex.getMessage(), 400);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String conflict(Model model, HttpServletRequest request, RedirectAttributes flash,
                            jakarta.servlet.http.HttpServletResponse response) {
        return volverAlFormulario(request, flash, model, response,
                "La operación entra en conflicto con datos existentes. Revisá que el DNI, email o código no estén registrados.", 409);
    }
}
