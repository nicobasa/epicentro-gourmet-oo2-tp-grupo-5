package com.epicentrogourmet.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.*;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutorizacionWebTest {
    @Autowired MockMvc mvc;
    @Autowired FestivalRepository festivales;
    @Autowired UnidadVentaRepository unidades;
    @Autowired EmpleadoRepository empleados;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired AsignacionLaboralRepository asignaciones;
    @Autowired PasswordEncoder encoder;
    @Autowired com.epicentrogourmet.test.service.PedidoService pedidosService;
    @Autowired com.epicentrogourmet.test.service.CierreCajaService cierresService;
    @Autowired PlatoRepository platos;
    @Autowired DetallePedidoRepository detalles;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired com.epicentrogourmet.test.service.HistorialService historialService;

    @BeforeEach
    void prepararUnidadResponsableParaVistas() {
        Festival festival = festivales.findAll().getFirst();
        if (usuarios.findByUsername("responsable-vista").isPresent()) return;
        Usuario usuario = new Usuario(); usuario.setUsername("responsable-vista");
        usuario.setPassword(encoder.encode("test-only")); usuario.setRol(roles.findByNombre("RESPONSABLE").orElseThrow());
        usuarios.save(usuario);
        FoodTruck unidad = new FoodTruck(); unidad.setNombreComercial("Unidad de pruebas"); unidad.setCodigo("FT12345678");
        unidad.setSuperficie(20.0); unidad.setPatente("AA123BB");
        unidades.save(unidad);
        Empleado responsable = new Empleado(); responsable.setNombre("Demo"); responsable.setApellido("Responsable");
        responsable.setDni(88000111L); responsable.setFechaNacimiento(java.time.LocalDate.of(1980, 1, 1));
        responsable.setFechaIngreso(java.time.LocalDate.of(2020, 1, 1)); responsable.setSueldoBase(100000.0);
        responsable.setActivo(true); responsable.setUnidadVenta(unidad); responsable.setUsuario(usuario);
        empleados.save(responsable); unidad.setResponsable(responsable); unidades.save(unidad);
        festival.getUnidadesDeVenta().add(unidad); festivales.save(festival);
        unidad.getFestivales().add(festival);
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleadoNoAccedeAReportesAdminNiPedidos() throws Exception {
        mvc.perform(get("/admin/panel")).andExpect(status().isForbidden());
        mvc.perform(get("/pedidos/nuevo")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void cocineroConRolEmpleadoNoAccedeAlCierreDeCaja() throws Exception {
        mvc.perform(get("/caja")).andExpect(status().isForbidden());
        mvc.perform(post("/caja/cerrar").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void mantieneProteccionCsrfEnOperacionesDeEscritura() throws Exception {
        mvc.perform(post("/pedidos/guardar")).andExpect(status().isForbidden());
    }

    @Test
    void loginYPaginaDeInicioSonPublicos() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPanelDeReportesRenderiza() throws Exception {
        mvc.perform(get("/admin/panel")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "responsable-vista", roles = "RESPONSABLE")
    void panelResponsableRenderizaConDatosDeSuUnidad() throws Exception {
        mvc.perform(get("/responsable/panel")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "cajero-vista", roles = {"EMPLEADO", "CAJERO"})
    void empleadoVeSoloSuHistorialYReciboPdfPropio() throws Exception {
        Festival festival = festivales.findAll().getFirst();
        UnidadVenta unidad = unidades.findAll().stream().filter(u -> "FT12345678".equals(u.getCodigo())).findFirst().orElseThrow();
        Usuario usuario = new Usuario(); usuario.setUsername("cajero-vista"); usuario.setPassword(encoder.encode("test-only"));
        usuario.setRol(roles.findByNombre("EMPLEADO").orElseThrow()); usuarios.save(usuario);
        Cajero cajero = new Cajero(); cajero.setNombre("Demo"); cajero.setApellido("Cajero"); cajero.setDni(88000112L);
        cajero.setFechaNacimiento(java.time.LocalDate.of(1980, 1, 1)); cajero.setFechaIngreso(java.time.LocalDate.of(2020, 1, 1));
        cajero.setSueldoBase(100000.0); cajero.setActivo(true); cajero.setUnidadVenta(unidad); cajero.setUsuario(usuario); cajero.setTurno("Manana");
        empleados.save(cajero);
        AsignacionLaboral asignacion = new AsignacionLaboral(); asignacion.setEmpleado(cajero); asignacion.setFestival(festival);
        asignacion.setUnidad(unidad); asignacion.setFechaInicio(festival.getFechaInicio()); asignacion.setFechaFin(festival.getFechaFin());
        asignaciones.save(asignacion);
        mvc.perform(get("/empleado/panel")).andExpect(status().isOk());
        mvc.perform(get("/recibos/" + festival.getId() + ".pdf"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO", "CAJERO"})
    void inicioCajeroTieneUnSoloTextoYUnSoloAccesoALaInformacion() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals(1, org.springframework.util.StringUtils.countOccurrencesOf(result.getResponse().getContentAsString(), "href=\"/empleado/panel\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cajero del Staff")));
    }

    @Test
    @WithMockUser(username = "responsable-vista", roles = "RESPONSABLE")
    void dniDesbordadoMuestraAlertaYConservaElFormulario() throws Exception {
        UnidadVenta unidad = unidades.findAll().stream().filter(u -> "FT12345678".equals(u.getCodigo())).findFirst().orElseThrow();
        mvc.perform(post("/responsable/empleados/alta").with(csrf())
                .param("unidadId",unidad.getId().toString()).param("tipoEmpleado","CAJERO")
                .param("nombre","Peña").param("apellido","Muñoz").param("dni","2451234223213111111111111")
                .param("fechaNacimiento","1990-01-01").param("emailUsuario","demo@example.test")
                .param("turno","Mañana").param("festivalIds",festivales.findAll().getFirst().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("responsable-panel"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("El DNI debe tener entre 7 y 8 dígitos")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attribute("altaForm",org.hamcrest.Matchers.hasProperty("nombre",org.hamcrest.Matchers.is("Peña"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("name=\"fechaIngreso\""))));
    }

    @Test
    @WithMockUser(username = "responsable-vista", roles = "RESPONSABLE")
    void fechasFueraDelFestivalMuestranAvisoEnElMismoPanel() throws Exception {
        Festival festival = festivales.findAll().getFirst();
        mvc.perform(get("/responsable/panel").param("festivalId",festival.getId().toString())
                .param("desde",festival.getFechaInicio().minusDays(1).toString()).param("hasta",festival.getFechaFin().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("El rango debe estar dentro")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fechas del festival:")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"filtroTipo\"")));
    }

    @Test
    @WithMockUser(username = "responsable-vista", roles = "RESPONSABLE")
    void valoresNumericosInvalidosVuelvenAlFormularioConAlerta() throws Exception {
        mvc.perform(post("/responsable/platos/alta").with(csrf()).param("unidadId","invalida")
                .param("nombre","Plato").param("precio","1").param("costo","0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash().attributeExists("error"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/responsable/panel"));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void ajustesExigeSesionYCsrf() throws Exception {
        mvc.perform(get("/cuenta/ajustes")).andExpect(status().isOk());
        mvc.perform(post("/cuenta/password").param("actual","x").param("nueva","y").param("confirmacion","y"))
                .andExpect(status().isForbidden());
    }

    @Test
    void ventaCierreYRechazoPosteriorFuncionanConPersistenciaReal() {
        Festival festival = festivales.findAll().getFirst();
        festival.setFechaInicio(java.time.LocalDate.now()); festival.setFechaFin(java.time.LocalDate.now().plusDays(5));
        festivales.save(festival);
        UnidadVenta unidad = unidades.findAll().stream().filter(u -> "FT12345678".equals(u.getCodigo())).findFirst().orElseThrow();
        Plato plato = new Plato(); plato.setNombre("Plato de prueba"); plato.setPrecioVenta(100.0); plato.setCostoProduccion(30.0); platos.save(plato);
        unidad.getPlatos().add(plato); unidades.save(unidad);
        Cajero cajero = new Cajero(); cajero.setNombre("Peña"); cajero.setApellido("Muñoz"); cajero.setDni(88000199L);
        cajero.setFechaNacimiento(java.time.LocalDate.of(1990,1,1)); cajero.setFechaIngreso(java.time.LocalDate.now());
        cajero.setUnidadVenta(unidad); cajero.setTurno("Mañana"); cajero.setActivo(true); empleados.save(cajero);
        var pedido = pedidosService.registrar(festival.getId(),unidad.getId(),plato.getId(),2,null,null,"responsable-vista",false);
        org.junit.jupiter.api.Assertions.assertEquals(30.0,detalles.findByPedidoId(pedido.getId()).getFirst().getCostoUnitario());
        var cierre = cierresService.cerrarHoy(cajero);
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("200.00"),cierre.getTotal());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,()->pedidosService.registrar(festival.getId(),unidad.getId(),plato.getId(),1,null,null,"responsable-vista",false));
    }

    @Test
    @WithMockUser(username = "responsable-vista", roles = "RESPONSABLE")
    void reactivarDesdeElPanelConservaLaCuentaYMuestraCategoria() throws Exception {
        UnidadVenta unidad = unidades.findAll().stream().filter(u -> "FT12345678".equals(u.getCodigo())).findFirst().orElseThrow();
        Usuario usuario = new Usuario(); usuario.setUsername("lavaplatos@example.test"); usuario.setPassword(encoder.encode("prueba123")); usuario.setRol(roles.findByNombre("EMPLEADO").orElseThrow()); usuarios.save(usuario);
        Cocinero empleado = new Cocinero(); empleado.setNombre("Peña"); empleado.setApellido("Muñoz"); empleado.setDni(88000198L);
        empleado.setFechaNacimiento(java.time.LocalDate.of(1990,1,1)); empleado.setFechaIngreso(java.time.LocalDate.of(2020,1,1));
        empleado.setUnidadVenta(unidad); empleado.setUsuario(usuario); empleado.setCategoria(CategoriaCocina.LAVAPLATOS); empleado.setActivo(false); empleados.save(empleado);
        mvc.perform(get("/responsable/panel"))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Personal inactivo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Lavaplatos")));
        mvc.perform(post("/responsable/empleados/reactivar/"+empleado.getId()).with(csrf()).param("unidadId",unidad.getId().toString()))
                .andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertTrue(empleados.findById(empleado.getId()).orElseThrow().isActivo());
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("prueba123",usuario.getPassword()));
    }

    @Test
    void historialCargaElSubtipoRealAntesDeCongelarLaLiquidacion() {
        Festival festival = festivales.findAll().getFirst();
        UnidadVenta unidad = unidades.findAll().stream().filter(u -> "FT12345678".equals(u.getCodigo())).findFirst().orElseThrow();
        Cocinero cocinero = new Cocinero(); cocinero.setNombre("Cocinero"); cocinero.setApellido("Prueba"); cocinero.setDni(88000197L);
        cocinero.setFechaNacimiento(java.time.LocalDate.of(1990,1,1)); cocinero.setFechaIngreso(java.time.LocalDate.of(2020,1,1));
        cocinero.setUnidadVenta(unidad); cocinero.setCategoria(CategoriaCocina.COCINERO); empleados.save(cocinero);
        AsignacionLaboral a = new AsignacionLaboral(); a.setEmpleado(cocinero); a.setFestival(festival); a.setUnidad(unidad);
        a.setFechaInicio(festival.getFechaInicio()); a.setFechaFin(festival.getFechaFin()); asignaciones.save(a);
        Long id = a.getId(); entityManager.flush(); entityManager.clear();
        historialService.conservarValoresExistentes();
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("125000.00"),asignaciones.findById(id).orElseThrow().getSueldoLiquidado());
    }
}
