package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Empleado;
import com.epicentrogourmet.test.entity.Festival;
import com.epicentrogourmet.test.repository.FestivalRepository;
import com.epicentrogourmet.test.repository.AsignacionLaboralRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class ReciboSueldoService {
    private final EmpleadoService empleados;
    private final FestivalRepository festivales;
    private final AsignacionLaboralRepository asignaciones;
    private final ReporteService reportes;

    public ReciboSueldoService(EmpleadoService empleados, FestivalRepository festivales,
                               AsignacionLaboralRepository asignaciones, ReporteService reportes) {
        this.empleados = empleados;
        this.festivales = festivales;
        this.asignaciones = asignaciones;
        this.reportes = reportes;
    }

    @Transactional(readOnly = true)
    public byte[] generar(String username, Long festivalId) {
        Empleado empleado = empleados.traerPorUsername(username);
        if (empleado == null || !empleado.isActivo()) throw new AccessDeniedException("No hay un empleado activo asociado a la cuenta.");
        Festival festival = festivales.findById(festivalId).orElseThrow(() -> new IllegalArgumentException("Festival inexistente."));
        var asignacion = asignaciones.findByEmpleadoIdAndFestivalIdAndUnidadId(empleado.getId(), festivalId,
                empleado.getUnidadVenta().getId()).orElseThrow(() -> new AccessDeniedException("No podés descargar recibos de un festival no asignado."));
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                stream.newLineAtOffset(60, 740);
                stream.showText("Epicentro Gourmet - Recibo de haberes");
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(0, -36);
                stream.showText("Empleado: " + safe(empleado.getNombre()) + " " + safe(empleado.getApellido()));
                stream.newLineAtOffset(0, -22);
                stream.showText("DNI: " + empleado.getDni());
                stream.newLineAtOffset(0, -22);
                stream.showText("Unidad: " + safe(empleado.getUnidadVenta().getNombreComercial()) + " (" + safe(empleado.getUnidadVenta().getCodigo()) + ")");
                stream.newLineAtOffset(0, -22);
                stream.showText("Festival: " + safe(festival.getNombre()));
                stream.newLineAtOffset(0, -22);
                stream.showText("Período: " + asignacion.getFechaInicio().format(DateTimeFormatter.ISO_DATE) + " a " + asignacion.getFechaFin().format(DateTimeFormatter.ISO_DATE));
                stream.newLineAtOffset(0, -22);
                stream.showText("Días inclusive: " + (java.time.temporal.ChronoUnit.DAYS.between(asignacion.getFechaInicio(), asignacion.getFechaFin()) + 1));
                stream.newLineAtOffset(0, -34);
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                stream.showText("Total liquidado: $ " + reportes.sueldo(asignacion).toPlainString());
                stream.endText();
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el recibo PDF.");
        }
    }

    private static String safe(String text) { return text == null ? "-" : text.replaceAll("[\\r\\n]", " "); }
}
