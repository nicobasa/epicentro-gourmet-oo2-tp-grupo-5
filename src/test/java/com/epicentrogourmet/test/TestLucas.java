package com.epicentrogourmet.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.epicentrogourmet.test.entity.DetallePedido;
import com.epicentrogourmet.test.entity.Plato;
import com.epicentrogourmet.test.entity.Pedido;
import com.epicentrogourmet.test.service.DetallePedidoService;
import com.epicentrogourmet.test.service.PlatoService;
import com.epicentrogourmet.test.service.PedidoService;

@SpringBootTest
@Transactional
public class TestLucas {

    @Autowired
    private PlatoService platoABM;

    @Autowired
    private PedidoService pedidoABM;

    @Autowired
    private DetallePedidoService detallePedidoABM;

    @Test
    public void generarInforme() {



        // ============================================================
        // 1. INFORME DE PLATOS
        // ============================================================

        List<Plato> platos = platoABM.traer();

        System.out.println("========== INFORME DE PLATOS ==========");

        if (platos.isEmpty()) {

            System.out.println("No hay platos registrados.");

        } else {

            double sumaPrecios = 0;
            double sumaGanancias = 0;

            Plato platoMasCaro = platos.get(0);
            Plato platoMayorGanancia = platos.get(0);
            Plato platoMejorMargen = platos.get(0);

            for (Plato plato : platos) {

                double precio = plato.getPrecioVenta();
                double costo = plato.getCostoProduccion();

                double ganancia = precio - costo;

                double margen = 0;

                if (precio > 0) {
                    margen = (ganancia / precio) * 100;
                }

                System.out.println(
                        "ID: " + plato.getId()
                                + " | " + plato.getNombre()
                                + " | Precio: $" + precio
                                + " | Costo: $" + costo
                                + " | Ganancia: $" + ganancia
                                + " | Margen: "
                                + String.format("%.2f", margen) + "%"
                );


                // Acumular datos
                sumaPrecios += precio;
                sumaGanancias += ganancia;


                // Plato más caro
                if (precio > platoMasCaro.getPrecioVenta()) {
                    platoMasCaro = plato;
                }


                // Plato con mayor ganancia
                double gananciaActualMayor =
                        platoMayorGanancia.getPrecioVenta()
                                - platoMayorGanancia.getCostoProduccion();

                if (ganancia > gananciaActualMayor) {
                    platoMayorGanancia = plato;
                }


                // Plato con mejor margen
                double gananciaMejorMargen =
                        platoMejorMargen.getPrecioVenta()
                                - platoMejorMargen.getCostoProduccion();

                double margenMejor = 0;

                if (platoMejorMargen.getPrecioVenta() > 0) {

                    margenMejor =
                            (gananciaMejorMargen
                                    / platoMejorMargen.getPrecioVenta()) * 100;
                }

                if (margen > margenMejor) {
                    platoMejorMargen = plato;
                }
            }


            // Promedios
            double precioPromedio =
                    sumaPrecios / platos.size();

            double gananciaPromedio =
                    sumaGanancias / platos.size();


            // Ganancia mayor
            double gananciaMayor =
                    platoMayorGanancia.getPrecioVenta()
                            - platoMayorGanancia.getCostoProduccion();


            // Mejor margen
            double gananciaMejor =
                    platoMejorMargen.getPrecioVenta()
                            - platoMejorMargen.getCostoProduccion();

            double mejorMargen = 0;

            if (platoMejorMargen.getPrecioVenta() > 0) {

                mejorMargen =
                        (gananciaMejor
                                / platoMejorMargen.getPrecioVenta()) * 100;
            }


            System.out.println();
            System.out.println("========== ESTADISTICAS DE PLATOS ==========");

            System.out.println(
                    "Cantidad de platos: "
                            + platos.size()
            );

            System.out.println(
                    "Precio promedio: $"
                            + String.format("%.2f", precioPromedio)
            );

            System.out.println(
                    "Ganancia promedio: $"
                            + String.format("%.2f", gananciaPromedio)
            );


            System.out.println();
            System.out.println("========== DESTACADOS ==========");

            System.out.println(
                    "Plato más caro: "
                            + platoMasCaro.getNombre()
                            + " ($"
                            + platoMasCaro.getPrecioVenta()
                            + ")"
            );

            System.out.println(
                    "Mayor ganancia: "
                            + platoMayorGanancia.getNombre()
                            + " ($"
                            + String.format("%.2f", gananciaMayor)
                            + ")"
            );

            System.out.println(
                    "Mejor margen: "
                            + platoMejorMargen.getNombre()
                            + " ("
                            + String.format("%.2f", mejorMargen)
                            + "%)"
            );
        }



        // ============================================================
        // 2. INFORME DE PEDIDOS
        // ============================================================

        List<Pedido> pedidos = pedidoABM.traer();

        System.out.println();
        System.out.println("========== INFORME DE PEDIDOS ==========");

        if (pedidos.isEmpty()) {

            System.out.println("No hay pedidos registrados.");

        } else {

            for (Pedido pedido : pedidos) {

                System.out.println(
                        "ID Pedido: "
                                + pedido.getId()
                                + " | Fecha: "
                                + pedido.getFechaTransaccion()
                );
            }

            System.out.println(
                    "Cantidad de pedidos: "
                            + pedidos.size()
            );
        }



        // ============================================================
        // 3. INFORME DE DETALLES DE PEDIDOS
        // ============================================================

        List<DetallePedido> detalles =
                detallePedidoABM.traer();

        System.out.println();
        System.out.println("========== DETALLES DE PEDIDOS ==========");

        if (detalles.isEmpty()) {

            System.out.println(
                    "No hay detalles de pedidos registrados."
            );

        } else {

            for (DetallePedido detalle : detalles) {

                Pedido pedido = detalle.getPedido();

                Long idPlato = null;

                if (detalle.getPlato() != null) {
                    idPlato = detalle.getPlato().getId();
                }


                System.out.println(
                        "Detalle ID: "
                                + detalle.getId()

                                + " | Pedido: "
                                + (pedido != null
                                ? pedido.getId()
                                : "Sin pedido")

                                + " | Plato ID: "
                                + (idPlato != null
                                ? idPlato
                                : "Sin plato")

                                + " | Cantidad: "
                                + detalle.getCantidad()

                                + " | Precio unitario: $"
                                + detalle.getPrecioUnitario()
                );
            }

            System.out.println(
                    "Cantidad de detalles: "
                            + detalles.size()
            );
        }



        // ============================================================
        // 4. ESTADISTICAS DE VENTAS POR PLATO
        // ============================================================

        System.out.println();
        System.out.println("========== ESTADISTICAS DE VENTAS ==========");

        if (detalles.isEmpty()) {

            System.out.println(
                    "No hay ventas para analizar."
            );

        } else {


            /*
             * ID DEL PLATO -> CANTIDAD TOTAL VENDIDA
             */
            Map<Long, Integer> unidadesPorPlato =
                    new HashMap<>();


            /*
             * ID DEL PLATO -> INGRESOS TOTALES
             */
            Map<Long, Double> ingresosPorPlato =
                    new HashMap<>();



            // --------------------------------------------------------
            // ACUMULAR VENTAS
            // --------------------------------------------------------

            for (DetallePedido detalle : detalles) {

                if (detalle.getPlato() == null) {
                    continue;
                }


                /*
                 * IMPORTANTE:
                 *
                 * Solamente obtenemos el ID del Plato relacionado.
                 * No hacemos getNombre(), getPrecioVenta(), etc.
                 * sobre este objeto porque puede ser un proxy LAZY.
                 */

                Long idPlato =
                        detalle.getPlato().getId();


                int cantidad =
                        detalle.getCantidad();


                double ingreso =
                        cantidad
                                * detalle.getPrecioUnitario();



                // Acumular cantidad
                int cantidadAnterior =
                        unidadesPorPlato.getOrDefault(
                                idPlato,
                                0
                        );

                unidadesPorPlato.put(
                        idPlato,
                        cantidadAnterior + cantidad
                );



                // Acumular ingresos
                double ingresoAnterior =
                        ingresosPorPlato.getOrDefault(
                                idPlato,
                                0.0
                        );

                ingresosPorPlato.put(
                        idPlato,
                        ingresoAnterior + ingreso
                );
            }



            // --------------------------------------------------------
            // MOSTRAR ESTADISTICAS
            // --------------------------------------------------------

            Plato platoMasVendido = null;
            Plato platoMayorIngreso = null;

            int mayorCantidad = 0;
            double mayorIngreso = 0;


            /*
             * Recorremos los platos que ya obtuvimos
             * correctamente con platoABM.traer().
             *
             * De esta forma evitamos el problema de LazyInitialization.
             */

            for (Plato plato : platos) {

                Long idPlato =
                        plato.getId();


                if (!unidadesPorPlato.containsKey(idPlato)) {
                    continue;
                }


                int unidades =
                        unidadesPorPlato.get(idPlato);


                double ingresos =
                        ingresosPorPlato.get(idPlato);



                System.out.println(
                        "Plato: "
                                + plato.getNombre()

                                + " | Unidades vendidas: "
                                + unidades

                                + " | Ingresos: $"
                                + String.format("%.2f", ingresos)
                );



                // Plato más vendido
                if (unidades > mayorCantidad) {

                    mayorCantidad = unidades;
                    platoMasVendido = plato;
                }



                // Plato que más ingresos generó
                if (ingresos > mayorIngreso) {

                    mayorIngreso = ingresos;
                    platoMayorIngreso = plato;
                }
            }



            // --------------------------------------------------------
            // RESULTADOS
            // --------------------------------------------------------

            System.out.println();
            System.out.println(
                    "========== DESTACADOS DE VENTAS =========="
            );


            if (platoMasVendido != null) {

                System.out.println(
                        "Plato más vendido: "
                                + platoMasVendido.getNombre()
                                + " ("
                                + mayorCantidad
                                + " unidades)"
                );
            }


            if (platoMayorIngreso != null) {

                System.out.println(
                        "Plato que más ingresos generó: "
                                + platoMayorIngreso.getNombre()
                                + " ($"
                                + String.format(
                                "%.2f",
                                mayorIngreso
                        )
                                + ")"
                );
            }
        }



        // ============================================================
        // 5. INGRESOS TOTALES
        // ============================================================

        double ingresosTotales = 0;


        for (DetallePedido detalle : detalles) {

            ingresosTotales +=
                    detalle.getCantidad()
                            * detalle.getPrecioUnitario();
        }


        System.out.println();
        System.out.println(
                "========== RESUMEN GENERAL =========="
        );


        System.out.println(
                "Pedidos registrados: "
                        + pedidos.size()
        );


        System.out.println(
                "Detalles registrados: "
                        + detalles.size()
        );


        System.out.println(
                "Ingresos totales: $"
                        + String.format(
                        "%.2f",
                        ingresosTotales
                )
        );


        System.out.println();
        System.out.println(
                "========== FIN DEL INFORME =========="
        );
    }
}
