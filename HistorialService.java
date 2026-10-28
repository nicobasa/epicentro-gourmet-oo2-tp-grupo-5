package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.repository.AsignacionLaboralRepository;
import com.epicentrogourmet.test.repository.DetallePedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialService {
    private final AsignacionLaboralRepository asignaciones;
    private final DetallePedidoRepository detalles;
    private final ReporteService reportes;
    private final com.epicentrogourmet.test.repository.FestivalRepository festivales;

    public HistorialService(AsignacionLaboralRepository asignaciones, DetallePedidoRepository detalles, ReporteService reportes,
                            com.epicentrogourmet.test.repository.FestivalRepository festivales) {
        this.asignaciones = asignaciones; this.detalles = detalles; this.reportes = reportes;
        this.festivales = festivales;
    }

    /** Completa una sola vez las columnas históricas agregadas a registros anteriores. */
    @Transactional
    public void conservarValoresExistentes() {
        for (var festival : festivales.findAll())
            for (var unidad : festival.getUnidadesDeVenta()) reportes.conservarCanon(unidad, festival);
        for (var detalle : detalles.findAll()) {
            if (detalle.getCostoUnitario() == null && detalle.getPlato() != null) {
                detalle.setCostoUnitario(detalle.getPlato().getCostoProduccion());
            }
        }
        for (var asignacion : asignaciones.findAll()) {
            if (asignacion.getSueldoLiquidado() == null) asignacion.setSueldoLiquidado(reportes.sueldo(asignacion));
        }
    }
}
