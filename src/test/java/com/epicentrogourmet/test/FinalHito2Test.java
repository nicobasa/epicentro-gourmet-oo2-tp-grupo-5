package com.epicentrogourmet.test;

import com.epicentrogourmet.test.dto.FestivalForm;
import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.*;
import com.epicentrogourmet.test.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class FinalHito2Test {
    @Autowired PlatoService platosService;
    @Autowired FestivalService festivalesService;
    @Autowired ReporteService reportes;
    @Autowired RegistroResponsableService registro;
    @Autowired HistorialService historial;
    @Autowired PlatoRepository platos;
    @Autowired UnidadVentaRepository unidades;
    @Autowired FestivalRepository festivales;
    @Autowired PedidoRepository pedidos;
    @Autowired DetallePedidoRepository detalles;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmpleadoRepository empleados;
    @Autowired AsignacionLaboralRepository asignaciones;
    @Autowired CanonUnidadFestivalRepository canones;
    @Autowired MockMvc mvc;
    @Autowired jakarta.persistence.EntityManager em;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
    static final AtomicInteger secuencia = new AtomicInteger(10000);
    Festival festival;
    FoodTruck a;
    PuestoDesarmable b;
    Plato compartido;

    @BeforeEach void preparar() {
        new org.springframework.transaction.support.TransactionTemplate(transactionManager).executeWithoutResult(status -> prepararDatos());
    }
    private void prepararDatos() {
        festival = festivales.findAll().getFirst();
        a = new FoodTruck(); a.setNombreComercial("Menú A"); a.setCodigo("FA"+String.format("%08d",secuencia.incrementAndGet()));
        a.setSuperficie(20.0); a.setPatente("AA123BB"); a.setRequiereElectricidad(true); unidades.save(a);
        b = new PuestoDesarmable(); b.setNombreComercial("Menú B"); b.setCodigo("FB"+String.format("%08d",secuencia.incrementAndGet()));
        b.setSuperficie(30.0); b.setTiempoMontaje(60); b.setCantidadCarpas(3); unidades.save(b);
        festival.getUnidadesDeVenta().addAll(List.of(a,b)); festivales.save(festival);
        a.getFestivales().add(festival); b.getFestivales().add(festival);
        compartido = new Plato(); compartido.setNombre("Empanada compartida"); compartido.setPrecioVenta(100.0); compartido.setCostoProduccion(30.0); platos.save(compartido);
        a.getPlatos().add(compartido); b.getPlatos().add(compartido); unidades.save(a); unidades.saveAndFlush(b);
    }

    @Test void editarCompartidoAislaLaUnidadYConservaLasVentas() {
        var pedido = venta(a);
        long cantidad = platos.count();
        var nuevo = platosService.modificarDeUnidad(a.getId(),compartido.getId(),"Empanada de A",120.0,40.0);
        em.flush(); em.clear();
        assertNotEquals(compartido.getId(),nuevo.getId()); assertEquals(cantidad+1,platos.count());
        var otro = unidades.findById(b.getId()).orElseThrow().getPlatos().getFirst();
        assertEquals("Empanada compartida",otro.getNombre()); assertEquals(100.0,otro.getPrecioVenta()); assertEquals(30.0,otro.getCostoProduccion());
        assertEquals(compartido.getId(),detalles.findByPedidoId(pedido.getId()).getFirst().getPlato().getId());
        assertEquals(new BigDecimal("200.00"),reportes.recaudacion(festival.getId(),a.getId()));
        long despues = platos.count();
        assertEquals(nuevo.getId(),platosService.modificarDeUnidad(a.getId(),nuevo.getId(),"Empanada de A",125.0,45.0).getId());
        assertEquals(despues,platos.count());
    }
    @Test void guardarSinCambiosNoDuplicaUnCompartido() {
        long cantidad=platos.count();
        assertEquals(compartido.getId(),platosService.modificarDeUnidad(a.getId(),compartido.getId(),compartido.getNombre(),100.0,30.0).getId());
        assertEquals(cantidad,platos.count());
    }
    @Test void platoExclusivoSinVentasSeModificaEnElMismoRegistro() {
        b.getPlatos().clear(); unidades.saveAndFlush(b);
        long cantidad=platos.count();
        assertEquals(compartido.getId(),platosService.modificarDeUnidad(a.getId(),compartido.getId(),"Nuevo nombre",110.0,35.0).getId());
        assertEquals(cantidad,platos.count());
    }
    @Test void unPlatoAjenoSeRechazaAntesDeModificar() {
        b.getPlatos().clear(); unidades.saveAndFlush(b);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->platosService.modificarDeUnidad(b.getId(),compartido.getId(),"Ajeno",200.0,100.0));
        assertEquals("Empanada compartida",platos.findById(compartido.getId()).orElseThrow().getNombre());
    }
    @Test @WithMockUser(roles="ADMIN")
    void quitarUnidadConVentasConservaRankingYRecaudacion() throws Exception {
        venta(a);
        var ranking=reportes.ranking(festival.getId()); var total=reportes.reporteVenta(festival.getId()).recaudacion();
        mvc.perform(post("/admin/festivales/"+festival.getId()+"/quitar-unidad/"+a.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attribute("error",containsString("pedidos históricos")));
        em.flush(); em.clear();
        assertEquals(ranking,reportes.ranking(festival.getId())); assertEquals(total,reportes.reporteVenta(festival.getId()).recaudacion());
        assertTrue(unidades.findDistinctByFestivalesId(festival.getId()).stream().anyMatch(u->u.getId().equals(a.getId())));
    }
    @Test void quitarUnidadSinActividadSePermite() {
        festivalesService.quitarUnidad(festival.getId(),a.getId());
        assertFalse(festival.getUnidadesDeVenta().stream().anyMatch(u->u.getId().equals(a.getId())));
        assertTrue(unidades.existsById(a.getId()));
    }
    @Test void asignacionesTambienImpidenPerderLaRelacion() {
        var empleado=new Empleado(); empleado.setNombre("Ana"); empleado.setApellido("Peña"); empleado.setDni(7654321L);
        empleado.setFechaNacimiento(LocalDate.of(1990,1,1)); empleado.setFechaIngreso(LocalDate.of(2020,1,1)); empleado.setUnidadVenta(a); empleados.save(empleado);
        var asignacion=new AsignacionLaboral(); asignacion.setEmpleado(empleado); asignacion.setFestival(festival); asignacion.setUnidad(a);
        asignacion.setFechaInicio(festival.getFechaInicio()); asignacion.setFechaFin(festival.getFechaFin()); asignaciones.save(asignacion);
        assertThrows(IllegalArgumentException.class,()->festivalesService.quitarUnidad(festival.getId(),a.getId()));
    }
    @Test void costosPosterioresNoCambianRentabilidadCanonNiVentas() {
        venta(a); historial.conservarValoresExistentes();
        var neto=reportes.rentabilidad(festival.getId(),a.getId(),null,null); var canon=reportes.canon(a,festival);
        var datos=form(); datos.setCostoPorSuperficie(new BigDecimal("900")); datos.setPlusElectricidad(new BigDecimal("8000"));
        festivalesService.modificar(festival.getId(),datos);
        platosService.modificarDeUnidad(a.getId(),compartido.getId(),"Plato actual",500.0,200.0);
        em.flush(); em.clear();
        assertEquals(neto,reportes.rentabilidad(festival.getId(),a.getId(),null,null));
        assertEquals(canon,reportes.canon(unidades.findById(a.getId()).orElseThrow(),festivales.findById(festival.getId()).orElseThrow()));
        assertEquals(new BigDecimal("200.00"),reportes.recaudacion(festival.getId(),a.getId()));
    }
    @Test void fechasHistoricasNoSePuedenMover() {
        venta(a); var datos=form(); datos.setFechaFin(festival.getFechaFin().plusDays(1));
        assertThrows(IllegalArgumentException.class,()->festivalesService.modificar(festival.getId(),datos));
    }
    @ParameterizedTest @ValueSource(strings={"costoPorSuperficie","costoPorMontaje","plusElectricidad","sueldoBase","montoAnioAntiguedad","plusCocinero","plusCocineroAyudante","plusLavaplatos"})
    @WithMockUser(roles="ADMIN")
    void cadaImporteNegativoEsRechazadoPorBackend(String campo) throws Exception {
        long cantidad=festivales.count(); var datos=form(); new BeanWrapperImpl(datos).setPropertyValue(campo,new BigDecimal("-1"));
        var peticion=post("/admin/festivales/alta").with(csrf());
        for(var descriptor:java.beans.Introspector.getBeanInfo(FestivalForm.class).getPropertyDescriptors()) {
            if(descriptor.getReadMethod()!=null && !descriptor.getName().equals("class")) {
                Object valor=descriptor.getReadMethod().invoke(datos); if(valor!=null) peticion.param(descriptor.getName(),valor.toString());
            }
        }
        mvc.perform(peticion).andExpect(status().is3xxRedirection()).andExpect(flash().attribute("error",containsString("negativo")));
        assertEquals(cantidad,festivales.count());
    }
    @Test void servicioTambienRechazaImportesInvalidosSinCambiosParciales() {
        String nombre=festival.getNombre(); var datos=form(); datos.setNombre("No debe persistir"); datos.setSueldoBase(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class,()->festivalesService.modificar(festival.getId(),datos));
        assertEquals(nombre,festival.getNombre());
        datos.setSueldoBase(new BigDecimal("10000000000"));
        assertThrows(IllegalArgumentException.class,()->festivalesService.crear(datos));
    }
    @ParameterizedTest @ValueSource(strings={"123","123456789","12abc456","999999999999999999999999999"})
    void dniResponsableInvalidoNoCreaNingunRegistro(String dni) throws Exception {
        long u=usuarios.count(),e=empleados.count(),v=unidades.count();
        mvc.perform(registroRequest(dni,"FOOD_TRUCK")).andExpect(status().isOk()).andExpect(view().name("registro-unidad"))
                .andExpect(content().string(containsString("DNI del Responsable debe tener entre 7 y 8")));
        assertEquals(u,usuarios.count()); assertEquals(e,empleados.count()); assertEquals(v,unidades.count());
    }
    @ParameterizedTest @ValueSource(longs={7654321L,76543210L})
    void dniValidoDeSieteYOchoDigitosSeRegistra(Long dni) {
        var emp=registro.registrar(festival.getId(),"FOOD_TRUCK","Nuevo",20.0,"DV"+String.format("%08d",secuencia.incrementAndGet()),"AB123CD",false,null,null,"Ana","Peña",dni,LocalDate.of(1990,1,1),"nueva"+dni,"PasswordDemo1!");
        assertEquals(dni,emp.getDni());
        assertEquals(100000.0,emp.getSueldoBase());
    }
    @Test @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void unFalloPosteriorAlUsuarioRevierteElRegistroCompleto() throws Exception {
        long u=usuarios.count(),e=empleados.count(),v=unidades.count();
        mvc.perform(registroRequest("78901234","TIPO_INVALIDO")).andExpect(status().isOk());
        assertEquals(u,usuarios.count()); assertEquals(e,empleados.count()); assertEquals(v,unidades.count());
    }

    @Test @WithMockUser(roles="ADMIN")
    void platoEstrellaEsElDeMasPedidosAunqueOtroVendaMasUnidades() throws Exception {
        Plato masUnidades = nuevoPlatoEstrella("Más unidades en un solo pedido");
        pedidoEstrella(festival,a,masUnidades,10);
        pedidoEstrella(festival,a,compartido,1);
        pedidoEstrella(festival,a,compartido,1);
        pedidoEstrella(festival,a,compartido,1);
        var estrella = reportes.platoEstrella(festival.getId(),a.getId());
        assertEquals(compartido.getId(),estrella.platoId());
        assertEquals(3L,estrella.cantidadVendida());
        mvc.perform(get("/responsable/panel").param("unidadId",a.getId().toString()).param("festivalId",festival.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Plato estrella: Empanada compartida (3 pedidos)")));
    }

    @Test void platoEstrellaCuentaUnaVezCadaPedidoAunqueRepitaElMismoPlato() {
        Plato repetido = nuevoPlatoEstrella("Tres ítems en un único pedido");
        var unico = pedidoEstrella(festival,a,repetido,1);
        detalleEstrella(unico,repetido,1);
        detalleEstrella(unico,repetido,1);
        pedidoEstrella(festival,a,compartido,1);
        pedidoEstrella(festival,a,compartido,1);
        var estrella = reportes.platoEstrella(festival.getId(),a.getId());
        assertEquals(compartido.getId(),estrella.platoId());
        assertEquals(2L,estrella.cantidadVendida());
    }

    @Test void platoEstrellaSoloCuentaPedidosDeLaUnidadYFestivalConsultados() {
        var datos = form(); datos.setNombre("Festival fuera de la consulta");
        Festival otroFestival = festivalesService.crear(datos);
        otroFestival.getUnidadesDeVenta().addAll(List.of(a,b)); festivales.save(otroFestival);
        Plato ajenoAlReporte = nuevoPlatoEstrella("Mayor presencia fuera del filtro");
        b.getPlatos().add(ajenoAlReporte); unidades.save(b);
        pedidoEstrella(festival,a,compartido,1);
        for (int i=0;i<3;i++) {
            pedidoEstrella(festival,b,ajenoAlReporte,1);
            pedidoEstrella(otroFestival,a,ajenoAlReporte,1);
        }
        var estrella = reportes.platoEstrella(festival.getId(),a.getId());
        assertEquals(compartido.getId(),estrella.platoId());
        assertEquals(1L,estrella.cantidadVendida());
    }

    @Test void empateDePlatoEstrellaSeResuelvePorElMenorId() {
        Plato mayorId = nuevoPlatoEstrella("ID posterior");
        pedidoEstrella(festival,a,mayorId,20);
        pedidoEstrella(festival,a,compartido,1);
        var estrella = reportes.platoEstrella(festival.getId(),a.getId());
        assertEquals(Math.min(compartido.getId(),mayorId.getId()),estrella.platoId());
        assertEquals(1L,estrella.cantidadVendida());
    }

    @Test void platoEstrellaSinPedidosDevuelveNull() {
        assertNull(reportes.platoEstrella(festival.getId(),a.getId()));
    }

    @Test void inicializarConCienMilNoReemplazaElSueldoConfiguradoDelFestival() {
        var datos = form(); datos.setNombre("Festival con base configurada");
        datos.setFechaInicio(LocalDate.now().plusDays(1)); datos.setFechaFin(LocalDate.now().plusDays(15));
        datos.setSueldoBase(new BigDecimal("123456.00"));
        Festival configurado = festivalesService.crear(datos);
        var responsable = registro.registrar(configurado.getId(),"FOOD_TRUCK","Base inicial",20.0,
                "BC"+String.format("%08d",secuencia.incrementAndGet()),"BC123DE",false,null,null,
                "Ana","Peña",78901236L,LocalDate.of(1990,1,1),"base.configurada","PasswordDemo1!");
        assertEquals(100000.0,responsable.getSueldoBase());
        assertEquals(new BigDecimal("123456.00"),configurado.getCosto().getSueldoBase());
        assertEquals(new BigDecimal("61728.00"),reportes.sueldo(responsable,configurado));
    }

    private Plato nuevoPlatoEstrella(String nombre) {
        Plato plato = new Plato(); plato.setNombre(nombre); plato.setPrecioVenta(100.0); plato.setCostoProduccion(30.0);
        platos.save(plato); a.getPlatos().add(plato); unidades.save(a); return plato;
    }
    private Pedido pedidoEstrella(Festival evento,UnidadVenta unidad,Plato plato,int cantidad) {
        Pedido pedido = new Pedido(); pedido.setFestival(evento); pedido.setUnidadDeVenta(unidad);
        pedido.setFechaTransaccion(evento.getFechaInicio().atTime(12,0)); pedidos.save(pedido);
        detalleEstrella(pedido,plato,cantidad); return pedido;
    }
    private void detalleEstrella(Pedido pedido,Plato plato,int cantidad) {
        DetallePedido item = new DetallePedido(); item.setPedido(pedido); item.setPlato(plato);
        item.setCantidad(cantidad); item.setPrecioUnitario(plato.getPrecioVenta()); item.setCostoUnitario(plato.getCostoProduccion());
        detalles.saveAndFlush(item);
    }
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder registroRequest(String dni,String tipo) {
        return post("/registro-unidad").with(csrf()).param("festivalId",festival.getId().toString()).param("tipoUnidad",tipo)
                .param("nombreComercial","Registro inválido").param("superficie","20").param("codigo","RV"+String.format("%08d",secuencia.incrementAndGet()))
                .param("patente","AA123BB").param("nombreResponsable","Ana").param("apellidoResponsable","Peña")
                .param("dniResponsable",dni).param("fechaNacimientoResponsable","1990-01-01").param("username","registro"+secuencia.incrementAndGet()).param("password","PasswordDemo1!");
    }
    private Pedido venta(UnidadVenta unidad) {
        var pedido=new Pedido(); pedido.setFestival(festival); pedido.setUnidadDeVenta(unidad); pedido.setFechaTransaccion(festival.getFechaInicio().atTime(12,0)); pedidos.save(pedido);
        var detalle=new DetallePedido(); detalle.setPedido(pedido); detalle.setPlato(compartido); detalle.setPrecioUnitario(100.0); detalle.setCostoUnitario(30.0); detalle.setCantidad(2); detalles.saveAndFlush(detalle);
        return pedido;
    }
    private FestivalForm form() {
        var d=new FestivalForm(); d.setNombre(festival.getNombre()); d.setTemporada(festival.getTemporada()); d.setFechaInicio(festival.getFechaInicio()); d.setFechaFin(festival.getFechaFin());
        var c=festival.getCosto(); d.setCostoPorSuperficie(c.getCostoPorSuperficie()); d.setCostoPorMontaje(c.getCostoPorMontaje()); d.setPlusElectricidad(c.getPlusElectricidad());
        d.setSueldoBase(c.getSueldoBase()); d.setMontoAnioAntiguedad(c.getMontoAnioAntiguedad()); d.setPlusCocinero(c.getPlusCocinero()); d.setPlusCocineroAyudante(c.getPlusCocineroAyudante()); d.setPlusLavaplatos(c.getPlusLavaplatos());
        return d;
    }
}
