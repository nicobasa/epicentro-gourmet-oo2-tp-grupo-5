package com.epicentrogourmet.test;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.RolRepository;
import com.epicentrogourmet.test.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Ejecutar en la consola para poblar MySQL real:
 * .\mvnw.cmd -Dtest=TestCargarBD test
 */
@SpringBootTest
@TestPropertySource(
        locations = "file:src/main/resources/application.properties",
        properties = {
                "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
                "spring.jpa.hibernate.ddl-auto=update"
        }
)
@Transactional
@Commit
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCargarBD {

    @Autowired
    private FestivalService festivalABM;

    @Autowired
    private UnidadVentaService unidadVentaABM;

    @Autowired
    private EmpleadoService empleadoABM;

    @Autowired
    private PlatoService platoABM;

    @Autowired
    private PedidoService pedidoABM;

    @Autowired
    private DetallePedidoService detallePedidoABM;

    @Autowired
    private UsuarioService usuarioABM;

    @Autowired
    private AsignacionLaboralService asignacionLaboralService;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void cargarBD() {

        System.out.println("========================================");
        System.out.println("  CARGA COMPLETA DE DATOS EN MYSQL REAL");
        System.out.println("========================================");

        // =====================================================
        // 0. ROLES Y USUARIO ADMINISTRADOR
        // =====================================================

        Rol rolAdmin = obtenerOCrearRol("ADMIN");
        Rol rolResponsable = obtenerOCrearRol("RESPONSABLE");
        Rol rolEmpleado = obtenerOCrearRol("EMPLEADO");

        obtenerOCrearUsuario("admin", "admin123", rolAdmin);

        // =====================================================
        // 1. FESTIVALES (CON SUS OBJETOS COSTO)
        // =====================================================

        List<Festival> festivales = festivalABM.traer();

        Festival festival1 = obtenerOCrearFestival(
                festivalABM,
                festivales,
                "Festival de la Primavera",
                "Primavera",
                LocalDate.of(2026, 9, 21),
                LocalDate.of(2026, 9, 28),
                500, 10, 2000, 100000);

        Festival festival2 = obtenerOCrearFestival(
                festivalABM,
                festivales,
                "Festival del Verano",
                "Verano",
                LocalDate.of(2026, 12, 15),
                LocalDate.of(2026, 12, 30),
                500, 10, 2000, 100000);

        Festival festival3 = obtenerOCrearFestival(
                festivalABM,
                festivales,
                "Festival Sabores de Buenos Aires",
                "Otoño",
                LocalDate.of(2027, 3, 1),
                LocalDate.of(2027, 4, 10),
                500, 10, 2000, 100000);

        Festival festival4 = obtenerOCrearFestival(
                festivalABM,
                festivales,
                "Festival Gourmet de Octubre",
                "Primavera",
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 30),
                500, 10, 2000, 100000);

        // =====================================================
        // 2. UNIDADES DE VENTA (6 FOOD TRUCKS Y 6 PUESTOS)
        // =====================================================

        List<UnidadVenta> unidades = unidadVentaABM.traer();

        FoodTruck unidad1 = obtenerOCrearFoodTruck(
                unidadVentaABM, unidades,
                "FT00000001", "Hamburguesas del Sur", 20.5, "AA123BB", true);

        FoodTruck unidad2 = obtenerOCrearFoodTruck(
                unidadVentaABM, unidades,
                "FT00000002", "Papas & Algo", 18.0, "AC456CD", false);

        PuestoDesarmable unidad3 = obtenerOCrearPuesto(
                unidadVentaABM, unidades,
                "PD00000001", "Sabores Criollos", 30.0, 3, 120);

        PuestoDesarmable unidad4 = obtenerOCrearPuesto(
                unidadVentaABM, unidades,
                "PD00000002", "Dulce Tentacion", 25.0, 2, 90);

        FoodTruck unidad5 = obtenerOCrearFoodTruck(
                unidadVentaABM, unidades,
                "FT00000003", "Pizza Express", 19.0, "AD111EF", true);

        FoodTruck unidad6 = obtenerOCrearFoodTruck(
                unidadVentaABM, unidades,
                "FT00000004", "Tacos del Barrio", 21.0, "AE222FG", true);

        FoodTruck unidad7 = obtenerOCrearFoodTruck(
                unidadVentaABM, unidades,
                "FT00000005", "La Parrilla Movil", 22.5, "AF333GH", false);

        FoodTruck unidad8 = obtenerOCrearFoodTruck(
                unidadVentaABM, unidades,
                "FT00000006", "Wok Oriental", 17.5, "AG444HI", true);

        PuestoDesarmable unidad9 = obtenerOCrearPuesto(
                unidadVentaABM, unidades,
                "PD00000003", "Pastas Caseras", 28.0, 3, 100);

        PuestoDesarmable unidad10 = obtenerOCrearPuesto(
                unidadVentaABM, unidades,
                "PD00000004", "Arepas Venezuela", 26.0, 2, 80);

        PuestoDesarmable unidad11 = obtenerOCrearPuesto(
                unidadVentaABM, unidades,
                "PD00000005", "Postres Artesanales", 24.0, 2, 75);

        PuestoDesarmable unidad12 = obtenerOCrearPuesto(
                unidadVentaABM, unidades,
                "PD00000006", "Sabores del Norte", 32.0, 4, 140);

        // =====================================================
        // 3. EMPLEADOS Y USUARIOS (12 RESPONSABLES + 12 STAFF)
        // =====================================================

        List<Empleado> empleados = empleadoABM.traer();

        Empleado empleado1 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Juan", "Perez", 30111222L,
                LocalDate.of(1990, 5, 10), LocalDate.of(2022, 3, 1),
                100000, "Manana", unidad1,
                "responsable@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado2 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Maria", "Gomez", 31222333L,
                LocalDate.of(1992, 8, 20), LocalDate.of(2023, 2, 15),
                100000, "Noche", unidad2,
                "maria.gomez@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado3 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Carlos", "Rodriguez", 32333444L,
                LocalDate.of(1988, 1, 15), LocalDate.of(2021, 6, 1),
                100000, "Parrilla", 150000, CategoriaCocina.COCINERO, unidad3,
                "carlos.rodriguez@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado4 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Laura", "Fernandez", 33444555L,
                LocalDate.of(1995, 11, 3), LocalDate.of(2023, 4, 10),
                100000, "Pasteleria", 150000, CategoriaCocina.COCINERO, unidad4,
                "laura.fernandez@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado5 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Pedro", "Lopez", 34555666L,
                LocalDate.of(1991, 2, 12), LocalDate.of(2024, 1, 10),
                100000, "Manana", unidad5,
                "pedro.lopez@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado6 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Sofia", "Martinez", 35666777L,
                LocalDate.of(1993, 7, 8), LocalDate.of(2024, 2, 5),
                100000, "Noche", unidad6,
                "sofia.martinez@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado7 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Diego", "Sanchez", 36777888L,
                LocalDate.of(1989, 9, 21), LocalDate.of(2023, 8, 12),
                100000, "Noche", unidad7,
                "diego.sanchez@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado8 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Valentina", "Diaz", 37888999L,
                LocalDate.of(1996, 4, 17), LocalDate.of(2024, 3, 2),
                100000, "Manana", unidad8,
                "valentina.diaz@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado9 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Martin", "Torres", 38999000L,
                LocalDate.of(1990, 12, 1), LocalDate.of(2023, 10, 20),
                100000, "Noche", unidad9,
                "martin.torres@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado10 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Federico", "Alvarez", 39000111L,
                LocalDate.of(1987, 3, 14), LocalDate.of(2021, 5, 4),
                100000, "Cocina Internacional", 150000, CategoriaCocina.COCINERO, unidad10,
                "federico.alvarez@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado11 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Camila", "Romero", 40111222L,
                LocalDate.of(1994, 6, 25), LocalDate.of(2023, 1, 15),
                100000, "Pasteleria", 150000, CategoriaCocina.COCINERO, unidad11,
                "camila.romero@epicentro.com", "resp123", rolResponsable, true);

        Empleado empleado12 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Nicolas", "Molina", 41222333L,
                LocalDate.of(1986, 10, 9), LocalDate.of(2020, 11, 8),
                100000, "Parrilla", 150000, CategoriaCocina.COCINERO, unidad12,
                "nicolas.molina@epicentro.com", "resp123", rolResponsable, true);

        // Staff en Unidad 1 ("Hamburguesas del Sur")
        Empleado staff1 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Lucia", "Paz", 42111000L,
                LocalDate.of(1998, 8, 20), LocalDate.of(2023, 5, 10),
                100000, "Manana", unidad1,
                "cajero@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff2 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Matias", "Silva", 42222000L,
                LocalDate.of(1993, 2, 11), LocalDate.of(2024, 1, 15),
                100000, "Hamburguesas Gourmet", 150000, CategoriaCocina.COCINERO, unidad1,
                "cocinero@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff3 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Paula", "Acosta", 42333000L,
                LocalDate.of(1999, 4, 12), LocalDate.of(2025, 2, 1),
                100000, "Minutas y Papas", 100000, CategoriaCocina.COCINERO_AYUDANTE, unidad1,
                "ayudante@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff4 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Ramiro", "Vega", 42444000L,
                LocalDate.of(2001, 9, 5), LocalDate.of(2025, 6, 1),
                100000, "Limpieza Cocina", 50000, CategoriaCocina.LAVAPLATOS, unidad1,
                "lavaplatos@epicentro.com", "emp123", rolEmpleado, true);

        // Empleado dado de baja lógica en Unidad 1 (DNI 42555000)
        obtenerOCrearCajero(
                empleadoABM, empleados,
                "Hernan", "Quiroga", 42555000L,
                LocalDate.of(1995, 3, 22), LocalDate.of(2023, 7, 1),
                100000, "Noche", unidad1,
                "inactivo@epicentro.com", "emp123", rolEmpleado, false);

        Empleado staff5 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Agustina", "Castro", 42333444L,
                LocalDate.of(1997, 1, 30), LocalDate.of(2025, 4, 1),
                100000, "Cocina Vegetariana", 100000, CategoriaCocina.COCINERO_AYUDANTE, unidad5,
                "agustina.castro@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff6 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Lucas", "Moreno", 43444555L,
                LocalDate.of(1992, 5, 19), LocalDate.of(2024, 7, 10),
                100000, "Noche", unidad6,
                "lucas.moreno@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff7 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Julieta", "Rojas", 44555666L,
                LocalDate.of(1995, 8, 11), LocalDate.of(2025, 5, 15),
                100000, "Cocina Mediterranea", 150000, CategoriaCocina.COCINERO, unidad7,
                "julieta.rojas@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff8 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Tomas", "Navarro", 45666777L,
                LocalDate.of(1988, 11, 27), LocalDate.of(2023, 9, 18),
                100000, "Manana", unidad8,
                "tomas.navarro@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff9 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Micaela", "Benitez", 46777888L,
                LocalDate.of(1999, 6, 14), LocalDate.of(2024, 11, 1),
                100000, "Empanadas y Horno", 100000, CategoriaCocina.COCINERO_AYUDANTE, unidad3,
                "micaela.benitez@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff10 = obtenerOCrearCajero(
                empleadoABM, empleados,
                "Gonzalo", "Herrera", 47888999L,
                LocalDate.of(1996, 12, 3), LocalDate.of(2022, 8, 20),
                100000, "Noche", unidad3,
                "gonzalo.herrera@epicentro.com", "emp123", rolEmpleado, true);

        Empleado staff11 = obtenerOCrearCocinero(
                empleadoABM, empleados,
                "Florencia", "Medina", 48999000L,
                LocalDate.of(2000, 1, 25), LocalDate.of(2025, 1, 10),
                100000, "Reposteria Fina", 50000, CategoriaCocina.LAVAPLATOS, unidad4,
                "florencia.medina@epicentro.com", "emp123", rolEmpleado, true);

        unidad1.setResponsable(empleado1);
        unidad2.setResponsable(empleado2);
        unidad3.setResponsable(empleado3);
        unidad4.setResponsable(empleado4);
        unidad5.setResponsable(empleado5);
        unidad6.setResponsable(empleado6);
        unidad7.setResponsable(empleado7);
        unidad8.setResponsable(empleado8);
        unidad9.setResponsable(empleado9);
        unidad10.setResponsable(empleado10);
        unidad11.setResponsable(empleado11);
        unidad12.setResponsable(empleado12);

        // =====================================================
        // 4. PLATOS (20 PLATOS EN EL MENU)
        // =====================================================

        List<Plato> platos = platoABM.traer();

        Plato plato1 = obtenerOCrearPlato(platoABM, platos, "Hamburguesa Clasica", 6500, 2800);
        Plato plato2 = obtenerOCrearPlato(platoABM, platos, "Hamburguesa Completa", 7500, 3400);
        Plato plato3 = obtenerOCrearPlato(platoABM, platos, "Papas Fritas", 3500, 1200);
        Plato plato4 = obtenerOCrearPlato(platoABM, platos, "Choripan", 4500, 1800);
        Plato plato5 = obtenerOCrearPlato(platoABM, platos, "Empanada de Carne", 2500, 900);
        Plato plato6 = obtenerOCrearPlato(platoABM, platos, "Empanada de Humita", 2500, 850);
        Plato plato7 = obtenerOCrearPlato(platoABM, platos, "Torta de Chocolate", 5000, 2200);
        Plato plato8 = obtenerOCrearPlato(platoABM, platos, "Cheesecake", 5500, 2500);
        Plato plato9 = obtenerOCrearPlato(platoABM, platos, "Pizza Muzzarella", 6000, 2500);
        Plato plato10 = obtenerOCrearPlato(platoABM, platos, "Pizza Napolitana", 7000, 3000);
        Plato plato11 = obtenerOCrearPlato(platoABM, platos, "Tacos de Carne", 5500, 2300);
        Plato plato12 = obtenerOCrearPlato(platoABM, platos, "Tacos Vegetarianos", 5000, 2100);
        Plato plato13 = obtenerOCrearPlato(platoABM, platos, "Pollo al Wok", 6500, 2800);
        Plato plato14 = obtenerOCrearPlato(platoABM, platos, "Fideos Salteados", 6000, 2400);
        Plato plato15 = obtenerOCrearPlato(platoABM, platos, "Ravioles Caseros", 7000, 2900);
        Plato plato16 = obtenerOCrearPlato(platoABM, platos, "Lasagna", 7500, 3200);
        Plato plato17 = obtenerOCrearPlato(platoABM, platos, "Arepa de Pollo", 4500, 1800);
        Plato plato18 = obtenerOCrearPlato(platoABM, platos, "Brownie", 4000, 1600);
        Plato plato19 = obtenerOCrearPlato(platoABM, platos, "Flan Casero", 3500, 1200);
        Plato plato20 = obtenerOCrearPlato(platoABM, platos, "Chocotorta", 5000, 2200);

        // =====================================================
        // 5. VINCULAR UNIDADES <-> PLATOS Y FESTIVALES
        // =====================================================

        unidad1.setPlatos(lista(plato1, plato2, plato3));
        unidad2.setPlatos(lista(plato2, plato3, plato4));
        unidad3.setPlatos(lista(plato4, plato5, plato6));
        unidad4.setPlatos(lista(plato5, plato6, plato7, plato8));
        unidad5.setPlatos(lista(plato1, plato9, plato10));
        unidad6.setPlatos(lista(plato2, plato11, plato12));
        unidad7.setPlatos(lista(plato3, plato4, plato13));
        unidad8.setPlatos(lista(plato5, plato6, plato14));
        unidad9.setPlatos(lista(plato7, plato15, plato16));
        unidad10.setPlatos(lista(plato8, plato17, plato18));
        unidad11.setPlatos(lista(plato19, plato20, plato1));
        unidad12.setPlatos(lista(plato2, plato7, plato8));

        unidad1.setFestivales(lista(festival1, festival2, festival4));
        unidad2.setFestivales(lista(festival1, festival3, festival4));
        unidad3.setFestivales(lista(festival1, festival2, festival3, festival4));
        unidad4.setFestivales(lista(festival2, festival3, festival4));
        unidad5.setFestivales(lista(festival1, festival3, festival4));
        unidad6.setFestivales(lista(festival1, festival2, festival4));
        unidad7.setFestivales(lista(festival2, festival3));
        unidad8.setFestivales(lista(festival1, festival2, festival3));
        unidad9.setFestivales(lista(festival1, festival3));
        unidad10.setFestivales(lista(festival1, festival2));
        unidad11.setFestivales(lista(festival2, festival3));
        unidad12.setFestivales(lista(festival1, festival2, festival3));

        actualizarUnidades(
                unidadVentaABM,
                unidad1, unidad2, unidad3, unidad4,
                unidad5, unidad6, unidad7, unidad8,
                unidad9, unidad10, unidad11, unidad12);

        festival1.setUnidadesDeVenta(lista(unidad1, unidad2, unidad3, unidad5, unidad6, unidad8, unidad9, unidad10, unidad12));
        festival2.setUnidadesDeVenta(lista(unidad1, unidad3, unidad4, unidad6, unidad7, unidad8, unidad10, unidad11, unidad12));
        festival3.setUnidadesDeVenta(lista(unidad2, unidad3, unidad4, unidad5, unidad7, unidad8, unidad9, unidad11, unidad12));
        festival4.setUnidadesDeVenta(lista(unidad1, unidad2, unidad3, unidad4, unidad5, unidad6));

        festivalABM.actualizar(festival1);
        festivalABM.actualizar(festival2);
        festivalABM.actualizar(festival3);
        festivalABM.actualizar(festival4);

        // =====================================================
        // 5b. ASIGNACIONES LABORALES (PARA LIQUIDACION Y RECIBOS PDF)
        // =====================================================

        List<Empleado> todosActivos = List.of(
                empleado1, empleado2, empleado3, empleado4, empleado5, empleado6,
                empleado7, empleado8, empleado9, empleado10, empleado11, empleado12,
                staff1, staff2, staff3, staff4, staff5, staff6, staff7, staff8, staff9, staff10, staff11
        );

        for (Empleado emp : todosActivos) {
            if (emp != null && emp.isActivo() && emp.getUnidadVenta() != null) {
                for (Festival fest : emp.getUnidadVenta().getFestivales()) {
                    if (emp.getFechaIngreso() == null || !emp.getFechaIngreso().isAfter(fest.getFechaFin())) {
                        asignacionLaboralService.asignar(
                                emp, fest, emp.getUnidadVenta(),
                                fest.getFechaInicio(), fest.getFechaFin());
                    }
                }
            }
        }

        // =====================================================
        // 6. PEDIDOS Y DETALLES DE PEDIDO
        // =====================================================

        List<Pedido> pedidos = pedidoABM.traer();

        Pedido pedido1 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 21, 12, 30), festival1, unidad1);
        Pedido pedido2 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 21, 13, 15), festival1, unidad1);
        Pedido pedido3 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 22, 14, 0), festival1, unidad2);
        Pedido pedido4 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 23, 19, 30), festival1, unidad3);
        Pedido pedido5 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 24, 12, 10), festival1, unidad5);
        Pedido pedido6 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 24, 13, 20), festival1, unidad6);
        Pedido pedido7 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 25, 19, 15), festival1, unidad8);
        Pedido pedido8 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 26, 20, 10), festival1, unidad9);
        Pedido pedido9 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 26, 21, 0), festival1, unidad10);
        Pedido pedido10 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 9, 27, 18, 30), festival1, unidad12);

        Pedido pedido11 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 16, 20, 15), festival2, unidad1);
        Pedido pedido12 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 17, 21, 0), festival2, unidad3);
        Pedido pedido13 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 18, 13, 40), festival2, unidad6);
        Pedido pedido14 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 19, 14, 10), festival2, unidad7);
        Pedido pedido15 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 19, 19, 45), festival2, unidad8);
        Pedido pedido16 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 21, 21, 10), festival2, unidad10);
        Pedido pedido17 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 22, 13, 0), festival2, unidad11);
        Pedido pedido18 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 23, 18, 50), festival2, unidad12);
        Pedido pedido19 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 24, 12, 30), festival2, unidad4);
        Pedido pedido20 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 12, 26, 20, 0), festival2, unidad7);

        Pedido pedido21 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 12, 12, 45), festival3, unidad2);
        Pedido pedido22 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 13, 18, 30), festival3, unidad4);
        Pedido pedido23 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 14, 12, 15), festival3, unidad5);
        Pedido pedido24 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 15, 14, 20), festival3, unidad7);
        Pedido pedido25 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 16, 19, 10), festival3, unidad8);
        Pedido pedido26 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 17, 20, 40), festival3, unidad11);
        Pedido pedido27 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 18, 21, 20), festival3, unidad12);
        Pedido pedido28 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 19, 13, 10), festival3, unidad9);
        Pedido pedido29 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 19, 18, 0), festival3, unidad3);
        Pedido pedido30 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2027, 3, 20, 19, 0), festival3, unidad5);

        Pedido pedido31 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 10, 20, 13, 0), festival4, unidad1);
        Pedido pedido32 = obtenerOCrearPedido(pedidoABM, pedidos, LocalDateTime.of(2026, 10, 20, 20, 30), festival4, unidad3);

        agregarDosDetalles(detallePedidoABM, pedido1, plato1, 2, plato3, 1);
        agregarDosDetalles(detallePedidoABM, pedido2, plato2, 1, plato3, 2);
        agregarDosDetalles(detallePedidoABM, pedido3, plato2, 1, plato4, 2);
        agregarDosDetalles(detallePedidoABM, pedido4, plato4, 2, plato6, 1);
        agregarDosDetalles(detallePedidoABM, pedido5, plato9, 2, plato10, 1);
        agregarDosDetalles(detallePedidoABM, pedido6, plato11, 2, plato12, 1);
        agregarDosDetalles(detallePedidoABM, pedido7, plato5, 2, plato14, 1);
        agregarDosDetalles(detallePedidoABM, pedido8, plato15, 1, plato16, 2);
        agregarDosDetalles(detallePedidoABM, pedido9, plato17, 2, plato18, 1);
        agregarDosDetalles(detallePedidoABM, pedido10, plato2, 1, plato7, 2);
        agregarDosDetalles(detallePedidoABM, pedido11, plato2, 1, plato3, 1);
        agregarDosDetalles(detallePedidoABM, pedido12, plato5, 3, plato6, 2);
        agregarDosDetalles(detallePedidoABM, pedido13, plato11, 1, plato12, 2);
        agregarDosDetalles(detallePedidoABM, pedido14, plato3, 2, plato13, 1);
        agregarDosDetalles(detallePedidoABM, pedido15, plato6, 1, plato14, 2);
        agregarDosDetalles(detallePedidoABM, pedido16, plato8, 1, plato17, 2);
        agregarDosDetalles(detallePedidoABM, pedido17, plato19, 2, plato20, 1);
        agregarDosDetalles(detallePedidoABM, pedido18, plato7, 1, plato8, 2);
        agregarDosDetalles(detallePedidoABM, pedido19, plato5, 2, plato7, 1);
        agregarDosDetalles(detallePedidoABM, pedido20, plato4, 1, plato13, 2);
        agregarDosDetalles(detallePedidoABM, pedido21, plato3, 2, plato4, 1);
        agregarDosDetalles(detallePedidoABM, pedido22, plato6, 2, plato8, 1);
        agregarDosDetalles(detallePedidoABM, pedido23, plato1, 1, plato10, 2);
        agregarDosDetalles(detallePedidoABM, pedido24, plato4, 2, plato13, 1);
        agregarDosDetalles(detallePedidoABM, pedido25, plato5, 1, plato14, 2);
        agregarDosDetalles(detallePedidoABM, pedido26, plato19, 1, plato20, 2);
        agregarDosDetalles(detallePedidoABM, pedido27, plato2, 2, plato8, 1);
        agregarDosDetalles(detallePedidoABM, pedido28, plato7, 1, plato15, 2);
        agregarDosDetalles(detallePedidoABM, pedido29, plato4, 1, plato6, 3);
        agregarDosDetalles(detallePedidoABM, pedido30, plato9, 2, plato10, 1);
        agregarDosDetalles(detallePedidoABM, pedido31, plato1, 3, plato2, 2);
        agregarDosDetalles(detallePedidoABM, pedido32, plato5, 6, plato4, 2);

        System.out.println();
        System.out.println("========================================");
        System.out.println("       CARGA FINALIZADA CON EXITO");
        System.out.println("========================================");
        System.out.println("Festivales:         " + festivalABM.traer().size());
        System.out.println("Unidades de venta:  " + unidadVentaABM.traer().size());
        System.out.println("Usuarios:           " + usuarioABM.traer().size());
        System.out.println("Empleados:          " + empleadoABM.traer().size());
        System.out.println("Platos:             " + platoABM.traer().size());
        System.out.println("Pedidos:            " + pedidoABM.traer().size());
        System.out.println("Detalles de pedido: " + detallePedidoABM.traer().size());
        System.out.println("========================================");
    }

    private Rol obtenerOCrearRol(String nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> rolRepository.save(new Rol(null, nombreRol)));
    }

    private Usuario obtenerOCrearUsuario(String username, String rawPassword, Rol rol) {
        Usuario existente = usuarioABM.traerPorUsername(username);
        if (existente != null) {
            return existente;
        }
        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setPassword(passwordEncoder.encode(rawPassword));
        nuevo.setRol(rol);
        return usuarioABM.agregar(nuevo);
    }

    private static Festival obtenerOCrearFestival(
            FestivalService abm,
            List<Festival> existentes,
            String nombre,
            String temporada,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            double costoPorSuperficie,
            double costoPorMontaje,
            double plusUsoElectricidad,
            double sueldoBase) {

        Festival festival = buscarFestival(existentes, nombre);

        Costo costo = (festival != null && festival.getCosto() != null)
                ? festival.getCosto()
                : new Costo();

        costo.setCostoPorSuperficie(BigDecimal.valueOf(costoPorSuperficie));
        costo.setCostoPorMontaje(BigDecimal.valueOf(costoPorMontaje));
        costo.setPlusElectricidad(BigDecimal.valueOf(plusUsoElectricidad));
        costo.setSueldoBase(BigDecimal.valueOf(sueldoBase));
        costo.setMontoAnioAntiguedad(BigDecimal.valueOf(5000));
        costo.setPlusCocinero(BigDecimal.valueOf(150000));
        costo.setPlusCocineroAyudante(BigDecimal.valueOf(100000));
        costo.setPlusLavaplatos(BigDecimal.valueOf(50000));

        if (festival == null) {
            festival = new Festival();
            festival.setNombre(nombre);
            festival.setTemporada(temporada);
            festival.setFechaInicio(fechaInicio);
            festival.setFechaFin(fechaFin);
            festival.setCosto(costo);

            abm.agregar(festival);
            existentes.add(festival);
        } else {
            festival.setTemporada(temporada);
            festival.setFechaInicio(fechaInicio);
            festival.setFechaFin(fechaFin);
            festival.setCosto(costo);

            abm.actualizar(festival);
        }

        return festival;
    }

    private static FoodTruck obtenerOCrearFoodTruck(
            UnidadVentaService abm,
            List<UnidadVenta> existentes,
            String codigo,
            String nombre,
            double superficie,
            String patente,
            boolean requiereElectricidad) {

        UnidadVenta existente = buscarUnidad(existentes, codigo);
        if (existente != null) {
            return (FoodTruck) existente;
        }

        FoodTruck unidad = new FoodTruck();
        unidad.setCodigo(codigo);
        unidad.setNombreComercial(nombre);
        unidad.setSuperficie(superficie);
        unidad.setPatente(patente);
        unidad.setRequiereElectricidad(requiereElectricidad);

        abm.agregar(unidad);
        existentes.add(unidad);
        return unidad;
    }

    private static PuestoDesarmable obtenerOCrearPuesto(
            UnidadVentaService abm,
            List<UnidadVenta> existentes,
            String codigo,
            String nombre,
            double superficie,
            int cantidadCarpas,
            int tiempoMontaje) {

        UnidadVenta existente = buscarUnidad(existentes, codigo);
        if (existente != null) {
            return (PuestoDesarmable) existente;
        }

        PuestoDesarmable unidad = new PuestoDesarmable();
        unidad.setCodigo(codigo);
        unidad.setNombreComercial(nombre);
        unidad.setSuperficie(superficie);
        unidad.setCantidadCarpas(cantidadCarpas);
        unidad.setTiempoMontaje(tiempoMontaje);

        abm.agregar(unidad);
        existentes.add(unidad);
        return unidad;
    }

    private Empleado obtenerOCrearCajero(
            EmpleadoService abm,
            List<Empleado> existentes,
            String nombre,
            String apellido,
            long dni,
            LocalDate fechaNacimiento,
            LocalDate fechaIngreso,
            double sueldoBase,
            String turno,
            UnidadVenta unidad,
            String username,
            String rawPassword,
            Rol rol,
            boolean activo) {

        Usuario usuario = obtenerOCrearUsuario(username, rawPassword, rol);

        Empleado existente = buscarEmpleadoPorDniOUsuario(existentes, dni, username);
        if (existente != null) {
            existente.setNombre(nombre);
            existente.setApellido(apellido);
            existente.setDni(dni);
            existente.setFechaNacimiento(fechaNacimiento);
            existente.setFechaIngreso(fechaIngreso);
            existente.setSueldoBase(sueldoBase);
            existente.setUsuario(usuario);
            existente.setUnidadVenta(unidad);
            existente.setActivo(activo);
            if (existente instanceof Cajero cajeroExistente) {
                cajeroExistente.setTurno(turno);
            }
            abm.actualizar(existente);
            return existente;
        }

        Cajero empleado = new Cajero();
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setDni(dni);
        empleado.setFechaNacimiento(fechaNacimiento);
        empleado.setFechaIngreso(fechaIngreso);
        empleado.setSueldoBase(sueldoBase);
        empleado.setTurno(turno);
        empleado.setUnidadVenta(unidad);
        empleado.setUsuario(usuario);
        empleado.setActivo(activo);

        abm.agregar(empleado);
        existentes.add(empleado);
        return empleado;
    }

    private Empleado obtenerOCrearCocinero(
            EmpleadoService abm,
            List<Empleado> existentes,
            String nombre,
            String apellido,
            long dni,
            LocalDate fechaNacimiento,
            LocalDate fechaIngreso,
            double sueldoBase,
            String especialidad,
            double plusCategoria,
            CategoriaCocina categoria,
            UnidadVenta unidad,
            String username,
            String rawPassword,
            Rol rol,
            boolean activo) {

        Usuario usuario = obtenerOCrearUsuario(username, rawPassword, rol);

        Empleado existente = buscarEmpleadoPorDniOUsuario(existentes, dni, username);
        if (existente != null) {
            existente.setNombre(nombre);
            existente.setApellido(apellido);
            existente.setDni(dni);
            existente.setFechaNacimiento(fechaNacimiento);
            existente.setFechaIngreso(fechaIngreso);
            existente.setSueldoBase(sueldoBase);
            existente.setUsuario(usuario);
            existente.setUnidadVenta(unidad);
            existente.setActivo(activo);
            if (existente instanceof Cocinero cocineroExistente) {
                cocineroExistente.setEspecialidadCulinaria(especialidad);
                cocineroExistente.setCategoria(categoria);
                cocineroExistente.setPlusCategoria(plusCategoria);
            }
            abm.actualizar(existente);
            return existente;
        }

        Cocinero empleado = new Cocinero();
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setDni(dni);
        empleado.setFechaNacimiento(fechaNacimiento);
        empleado.setFechaIngreso(fechaIngreso);
        empleado.setSueldoBase(sueldoBase);
        empleado.setEspecialidadCulinaria(especialidad);
        empleado.setPlusCategoria(plusCategoria);
        empleado.setCategoria(categoria);
        empleado.setUnidadVenta(unidad);
        empleado.setUsuario(usuario);
        empleado.setActivo(activo);

        abm.agregar(empleado);
        existentes.add(empleado);
        return empleado;
    }

    private static Plato obtenerOCrearPlato(
            PlatoService abm,
            List<Plato> existentes,
            String nombre,
            double precioVenta,
            double costoProduccion) {

        Plato plato = buscarPlato(existentes, nombre);
        if (plato == null) {
            plato = new Plato();
            plato.setNombre(nombre);
            plato.setPrecioVenta(precioVenta);
            plato.setCostoProduccion(costoProduccion);

            abm.agregar(plato);
            existentes.add(plato);
        }
        return plato;
    }

    private static Pedido obtenerOCrearPedido(
            PedidoService abm,
            List<Pedido> existentes,
            LocalDateTime fecha,
            Festival festival,
            UnidadVenta unidad) {

        Pedido pedido = buscarPedido(existentes, fecha, festival, unidad);
        if (pedido == null) {
            pedido = new Pedido();
            pedido.setFechaTransaccion(fecha);
            pedido.setFestival(festival);
            pedido.setUnidadDeVenta(unidad);

            abm.agregar(pedido);
            existentes.add(pedido);
        }
        return pedido;
    }

    private static void agregarDosDetalles(
            DetallePedidoService abm,
            Pedido pedido,
            Plato plato1,
            int cantidad1,
            Plato plato2,
            int cantidad2) {

        agregarDetalleSiNoExiste(abm, pedido, plato1, cantidad1, plato1.getPrecioVenta());
        agregarDetalleSiNoExiste(abm, pedido, plato2, cantidad2, plato2.getPrecioVenta());
    }

    private static void agregarDetalleSiNoExiste(
            DetallePedidoService abm,
            Pedido pedido,
            Plato plato,
            int cantidad,
            double precioUnitario) {

        List<DetallePedido> detalles = abm.traer();
        for (DetallePedido detalle : detalles) {
            if (detalle.getPedido() != null
                    && detalle.getPlato() != null
                    && detalle.getPedido().getId().equals(pedido.getId())
                    && detalle.getPlato().getId().equals(plato.getId())) {
                return;
            }
        }

        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setPlato(plato);
        detalle.setPedido(pedido);

        abm.agregar(detalle);
    }

    private static Festival buscarFestival(List<Festival> lista, String nombre) {
        for (Festival festival : lista) {
            if (festival.getNombre().equals(nombre)) {
                return festival;
            }
        }
        return null;
    }

    private static UnidadVenta buscarUnidad(List<UnidadVenta> lista, String codigo) {
        for (UnidadVenta unidad : lista) {
            if (unidad.getCodigo().equals(codigo)) {
                return unidad;
            }
        }
        return null;
    }

    private static Empleado buscarEmpleadoPorDniOUsuario(
            List<Empleado> lista,
            long dni,
            String username) {

        for (Empleado empleado : lista) {
            if (empleado.getDni() != null && empleado.getDni() == dni) {
                return empleado;
            }
            if (empleado.getUsuario() != null
                    && username.equalsIgnoreCase(empleado.getUsuario().getUsername())) {
                return empleado;
            }
        }
        return null;
    }

    private static Plato buscarPlato(List<Plato> lista, String nombre) {
        for (Plato plato : lista) {
            if (plato.getNombre().equals(nombre)) {
                return plato;
            }
        }
        return null;
    }

    private static Pedido buscarPedido(
            List<Pedido> lista,
            LocalDateTime fecha,
            Festival festival,
            UnidadVenta unidad) {

        for (Pedido pedido : lista) {
            if (pedido.getFechaTransaccion().equals(fecha)
                    && pedido.getFestival() != null
                    && pedido.getFestival().getId().equals(festival.getId())
                    && pedido.getUnidadDeVenta() != null
                    && pedido.getUnidadDeVenta().getId().equals(unidad.getId())) {
                return pedido;
            }
        }
        return null;
    }

    @SafeVarargs
    private static <T> ArrayList<T> lista(T... elementos) {
        return new ArrayList<>(Arrays.asList(elementos));
    }

    private static void actualizarUnidades(UnidadVentaService abm, UnidadVenta... unidades) {
        for (UnidadVenta unidad : unidades) {
            abm.actualizar(unidad);
        }
    }
}