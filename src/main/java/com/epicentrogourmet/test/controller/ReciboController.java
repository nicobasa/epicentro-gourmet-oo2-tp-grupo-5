package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.service.ReciboSueldoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recibos")
public class ReciboController {
    private final ReciboSueldoService recibos;
    public ReciboController(ReciboSueldoService recibos) { this.recibos = recibos; }

    @GetMapping("/{festivalId}.pdf")
    public ResponseEntity<byte[]> descargar(@PathVariable Long festivalId,
            org.springframework.security.core.Authentication authentication) {
        byte[] pdf = recibos.generar(authentication.getName(), festivalId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo-festival-" + festivalId + ".pdf")
                .body(pdf);
    }
}
