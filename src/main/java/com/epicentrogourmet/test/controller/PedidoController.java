package com.epicentrogourmet.test.controller;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.service.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;
    private final DetallePedidoService detallePedidoService;
    private final FestivalService festivalService;
    private final UnidadVentaService unidadVentaService;
    private final EmpleadoService empleadoService;

    public PedidoController(PedidoService pedidoService, DetallePedidoService detallePedidoService,
                            FestivalService festivalService, UnidadVentaService unidadVentaService,
                            EmpleadoService empleadoService) {
        this.pedidoService = pedidoService;
        this.detallePedidoService = detallePedidoService;
        this.festivalService = festivalService;
        this.unidadVentaService = unidadVentaService;
        this.empleadoService = empleadoService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioPedido(Authentication auth, Model model) {
        boolean admin = esAdmin(auth);
        List<UnidadVenta> unidades = admin ? unidadVentaService.traer() : List.of(unidadPropia(auth));
        Set<Long> idsUnidades = new HashSet<>();
        unidades.forEach(u -> idsUnidades.add(u.getId()));
        List<Festival> festivales = admin ? festivalService.traer()
                : unidades.getFirst().getFestivales();
        List<Pedido> pedidos = admin ? pedidoService.traer()
                : pedidoService.porUnidad(unidades.getFirst().getId());
        pedidos = pedidos.stream().filter(p -> p.getUnidadDeVenta() != null && idsUnidades.contains(p.getUnidadDeVenta().getId())).toList();
        Map<Long, BigDecimal> totales = new HashMap<>();
        Map<Long, String> resumenes = new HashMap<>();
        for (Pedido pedido : pedidos) {
            BigDecimal total = BigDecimal.ZERO;
            StringJoiner resumen = new StringJoiner(" + ");
            for (DetallePedido detalle : detallePedidoService.traer()) {
                if (detalle.getPedido() != null && pedido.getId().equals(detalle.getPedido().getId())
                        && detalle.getCantidad() != null && detalle.getPrecioUnitario() != null) {
                    total = total.add(BigDecimal.valueOf(detalle.getPrecioUnitario()).multiply(BigDecimal.valueOf(detalle.getCantidad())));
                    resumen.add(detalle.getCantidad() + "x " + detalle.getPlato().getNombre());
                }
            }
            totales.put(pedido.getId(), total);
            resumenes.put(pedido.getId(), resumen.toString());
        }
        model.addAttribute("festivales", festivales);
        model.addAttribute("unidades", unidades);
        model.addAttribute("platos", unidades.stream().flatMap(u -> u.getPlatos().stream()).distinct().toList());
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("totalesPorPedido", totales);
        model.addAttribute("resumenItemsPorPedido", resumenes);
        model.addAttribute("esAdmin", admin);
        return "pedido-form";
    }

    @PostMapping("/guardar")
    public String guardarPedido(@RequestParam Long festivalId, @RequestParam Long unidadVentaId,
                                @RequestParam Long plato1Id, @RequestParam Integer cantidad1,
                                @RequestParam(required = false) Long plato2Id,
                                @RequestParam(required = false) Integer cantidad2,
                                Authentication auth, RedirectAttributes flash) {
        try {
            Pedido pedido = pedidoService.registrar(festivalId, unidadVentaId, plato1Id, cantidad1,
                    plato2Id, cantidad2, auth.getName(), esAdmin(auth));
            flash.addFlashAttribute("mensajeExito", "Pedido #" + pedido.getId() + " registrado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/nuevo";
    }

    private UnidadVenta unidadPropia(Authentication auth) {
        Empleado actor = empleadoService.traerPorUsername(auth.getName());
        if (actor == null || !actor.isActivo() || actor.getUnidadVenta() == null) throw new AccessDeniedException("No hay una unidad de venta asignada a esta cuenta.");
        return actor.getUnidadVenta();
    }

    private boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
