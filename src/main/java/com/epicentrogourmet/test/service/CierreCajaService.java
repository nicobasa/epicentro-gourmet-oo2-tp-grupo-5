package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Cajero;
import com.epicentrogourmet.test.entity.CierreCaja;
import com.epicentrogourmet.test.entity.DetallePedido;
import com.epicentrogourmet.test.entity.Pedido;
import com.epicentrogourmet.test.repository.CierreCajaRepository;
import com.epicentrogourmet.test.repository.DetallePedidoRepository;
import com.epicentrogourmet.test.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CierreCajaService {
    private final CierreCajaRepository cierres;
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final com.epicentrogourmet.test.repository.UnidadVentaRepository unidades;

    public CierreCajaService(CierreCajaRepository cierres, PedidoRepository pedidos, DetallePedidoRepository detalles,
                            com.epicentrogourmet.test.repository.UnidadVentaRepository unidades) {
        this.cierres = cierres;
        this.pedidos = pedidos;
        this.detalles = detalles;
        this.unidades = unidades;
    }

    @Transactional
    public CierreCaja cerrarHoy(Cajero cajero) {
        if (cajero == null || !cajero.isActivo() || cajero.getUnidadVenta() == null) {
            throw new IllegalArgumentException("La cuenta no tiene una caja activa asignada.");
        }
        LocalDate hoy = LocalDate.now();
        unidades.bloquearPorId(cajero.getUnidadVenta().getId())
                .orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        if (cierres.findByUnidadIdAndFecha(cajero.getUnidadVenta().getId(), hoy).isPresent()) {
            throw new IllegalStateException("La caja de esta unidad ya fue cerrada hoy.");
        }
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.plusDays(1).atStartOfDay().minusNanos(1);
        BigDecimal total = BigDecimal.ZERO;
        for (Pedido pedido : pedidos.findByUnidadDeVentaIdAndFechaTransaccionBetween(cajero.getUnidadVenta().getId(), desde, hasta)) {
            for (DetallePedido detalle : detalles.findByPedidoId(pedido.getId())) {
                if (detalle.getCantidad() != null && detalle.getPrecioUnitario() != null) {
                    total = total.add(BigDecimal.valueOf(detalle.getPrecioUnitario())
                            .multiply(BigDecimal.valueOf(detalle.getCantidad())));
                }
            }
        }
        CierreCaja cierre = new CierreCaja();
        cierre.setUnidad(cajero.getUnidadVenta());
        cierre.setCajero(cajero);
        cierre.setFecha(hoy);
        cierre.setCerradoEn(LocalDateTime.now());
        cierre.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        return cierres.save(cierre);
    }
}
