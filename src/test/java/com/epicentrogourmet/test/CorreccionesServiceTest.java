package com.epicentrogourmet.test;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.*;
import com.epicentrogourmet.test.security.CustomUserDetailsService;
import com.epicentrogourmet.test.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorreccionesServiceTest {
    @Test
    void falloSmtpSeInformaYNoSeOcultaComoExito() {
        org.springframework.mail.javamail.JavaMailSender sender = mock(org.springframework.mail.javamail.JavaMailSender.class);
        @SuppressWarnings("unchecked")
        org.springframework.beans.factory.ObjectProvider<org.springframework.mail.javamail.JavaMailSender> provider = mock(org.springframework.beans.factory.ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        var service = new EmailService(provider);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"remitente","envio@example.test");
        doThrow(new org.springframework.mail.MailSendException("fallo de prueba")).when(sender).send(any(org.springframework.mail.SimpleMailMessage.class));
        assertThrows(IllegalStateException.class,()->service.enviarCredencialesEmpleado("demo@example.test","Peña Muñoz","temporal-prueba"));
    }

    @Test
    void envioSmtpIncluyeLaIndicacionDeCambiarLaPassword() {
        var sender = mock(org.springframework.mail.javamail.JavaMailSender.class);
        @SuppressWarnings("unchecked")
        org.springframework.beans.factory.ObjectProvider<org.springframework.mail.javamail.JavaMailSender> provider = mock(org.springframework.beans.factory.ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        var service = new EmailService(provider);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"remitente","envio@example.test");
        service.enviarCredencialesEmpleado("demo@example.test","Peña Muñoz","temporal-prueba");
        verify(sender).send(argThat((org.springframework.mail.SimpleMailMessage message)->message.getText().contains("Ajustes de mi cuenta")));
    }

    @Test
    void rentabilidadConservaHaberesDeInactivosYDeLaUnidadHistorica() {
        var asignaciones = mock(AsignacionLaboralRepository.class);
        var pedidos = mock(PedidoRepository.class);
        var unidades = mock(UnidadVentaRepository.class);
        var festivales = mock(FestivalRepository.class);
        var festival = festival();
        var unidad = unidad(festival);
        var empleado = new Empleado(); empleado.setActivo(false);
        var a = new AsignacionLaboral(); a.setEmpleado(empleado); a.setFestival(festival); a.setUnidad(unidad);
        a.setFechaInicio(festival.getFechaInicio().plusDays(10)); a.setFechaFin(festival.getFechaInicio().plusDays(19));
        a.setSueldoLiquidado(new BigDecimal("50000"));
        when(festivales.findById(1L)).thenReturn(Optional.of(festival));
        when(unidades.findById(2L)).thenReturn(Optional.of(unidad));
        when(asignaciones.findByUnidadIdAndFestivalId(2L,1L)).thenReturn(List.of(a));
        var service = new ReporteService(pedidos, mock(DetallePedidoRepository.class), unidades, asignaciones, festivales, mock(CanonUnidadFestivalRepository.class));
        assertEquals(new BigDecimal("-50000.00"), service.rentabilidad(1L,2L,null,null));
        assertEquals(new BigDecimal("0.00"), service.rentabilidad(1L,2L,festival.getFechaInicio(),festival.getFechaInicio().plusDays(4)));
        assertEquals(new BigDecimal("-25000.00"), service.rentabilidad(1L,2L,a.getFechaInicio(),a.getFechaInicio().plusDays(4)));
    }

    @Test
    void costoHistoricoNoCambiaAlModificarElMenu() {
        var asignaciones = mock(AsignacionLaboralRepository.class);
        var pedidos = mock(PedidoRepository.class);
        var detalles = mock(DetallePedidoRepository.class);
        var unidades = mock(UnidadVentaRepository.class);
        var festivales = mock(FestivalRepository.class);
        var festival = festival(); var unidad = unidad(festival);
        Plato plato = new Plato(); plato.setCostoProduccion(10.0);
        DetallePedido detalle = new DetallePedido(); detalle.setPlato(plato); detalle.setPrecioUnitario(100.0); detalle.setCantidad(2);
        detalle.conservarCostoDeVenta(); plato.setCostoProduccion(80.0);
        Pedido pedido = new Pedido(); pedido.setId(3L); pedido.setFestival(festival);
        when(festivales.findById(1L)).thenReturn(Optional.of(festival));
        when(unidades.findById(2L)).thenReturn(Optional.of(unidad));
        when(pedidos.findByUnidadDeVentaIdAndFechaTransaccionBetween(eq(2L),any(),any())).thenReturn(List.of(pedido));
        when(detalles.findByPedidoId(3L)).thenReturn(List.of(detalle));
        assertEquals(new BigDecimal("180.00"), new ReporteService(pedidos,detalles,unidades,asignaciones,festivales,mock(CanonUnidadFestivalRepository.class)).rentabilidad(1L,2L,null,null));
    }

    @Test
    void cambioDePasswordExigeLaActualYGuardaUnHash() {
        var usuarios = mock(UsuarioRepository.class); var encoder = new BCryptPasswordEncoder();
        var usuario = new Usuario(); usuario.setPassword(encoder.encode("temporal123"));
        when(usuarios.findByUsername("staff")).thenReturn(Optional.of(usuario));
        var service = new CuentaService(usuarios,encoder);
        assertThrows(IllegalArgumentException.class,()->service.cambiarPassword("staff","incorrecta","nuevaSegura123","nuevaSegura123"));
        assertThrows(IllegalArgumentException.class,()->service.cambiarPassword("staff","temporal123","nuevaSegura123","otra"));
        verify(usuarios,never()).save(any());
        service.cambiarPassword("staff","temporal123","nuevaSegura123","nuevaSegura123");
        assertTrue(encoder.matches("nuevaSegura123",usuario.getPassword()));
        assertFalse(encoder.matches("temporal123",usuario.getPassword()));
    }

    @Test
    void soloElCajeroDelStaffObtienePermisosDeCaja() {
        var usuarios = mock(UsuarioService.class); var empleados = mock(EmpleadoService.class);
        var cuenta = new Usuario(1L,"demo","hash",new Rol(1L,"EMPLEADO"));
        Cajero empleado = new Cajero(); empleado.setActivo(true);
        when(usuarios.traerPorUsername("demo")).thenReturn(cuenta);
        when(empleados.traerPorUsername("demo")).thenReturn(empleado);
        var service = new CustomUserDetailsService(usuarios,empleados);
        assertTrue(service.loadUserByUsername("demo").getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_CAJERO")));
        cuenta.setRol(new Rol(2L,"RESPONSABLE"));
        assertFalse(service.loadUserByUsername("demo").getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_CAJERO")));
    }

    @Test
    void cajaCerradaImpideVentasInclusoDelAdministrador() {
        var unidades = mock(UnidadVentaRepository.class); var festivales = mock(FestivalRepository.class);
        var cierres = mock(CierreCajaRepository.class); var pedidos = mock(PedidoRepository.class);
        var festival = festival(); festival.setFechaInicio(LocalDate.now()); festival.setFechaFin(LocalDate.now().plusDays(5));
        var unidad = unidad(festival);
        when(festivales.findById(1L)).thenReturn(Optional.of(festival));
        when(unidades.bloquearPorId(2L)).thenReturn(Optional.of(unidad));
        when(cierres.findByUnidadIdAndFecha(2L,LocalDate.now())).thenReturn(Optional.of(new CierreCaja()));
        var service = new PedidoService(pedidos,festivales,unidades,mock(PlatoRepository.class),mock(DetallePedidoRepository.class),mock(EmpleadoService.class),cierres);
        assertThrows(IllegalArgumentException.class,()->service.registrar(1L,2L,3L,1,null,null,"admin",true));
        verify(pedidos,never()).save(any());
    }

    @Test
    void altaUsaHoyYPermiteFestivalYaIniciado() {
        var empleados = mock(EmpleadoRepository.class); var usuarios = mock(UsuarioRepository.class);
        var roles = mock(RolRepository.class); var unidades = mock(UnidadVentaRepository.class);
        var festival = festival(); festival.setFechaInicio(LocalDate.now().minusDays(3)); festival.setFechaFin(LocalDate.now().plusDays(5));
        var unidad = unidad(festival); var asignaciones = mock(AsignacionLaboralService.class);
        when(unidades.findById(2L)).thenReturn(Optional.of(unidad));
        when(roles.findByNombre("EMPLEADO")).thenReturn(Optional.of(new Rol(1L,"EMPLEADO")));
        when(empleados.save(any())).thenAnswer(i->i.getArgument(0));
        var service = new EmpleadoGestionService(empleados,usuarios,roles,new BCryptPasswordEncoder(),asignaciones,mock(EmailService.class),unidades);
        var empleado = service.alta(2L,"CAJERO","Peña","Muñoz",32123456L,LocalDate.of(1990,1,1),"demo@example.test","Mañana",null,null,List.of(1L));
        assertEquals(LocalDate.now(),empleado.getFechaIngreso());
        assertEquals(100000.0,empleado.getSueldoBase());
        verify(asignaciones).asignar(empleado,festival,unidad,LocalDate.now(),festival.getFechaFin());
    }

    @Test
    void reactivarConservaCuentaIngresoEHistorial() {
        var empleados = mock(EmpleadoRepository.class); var unidad = new UnidadVenta(); unidad.setId(2L);
        var cuenta = new Usuario(1L,"demo","hash-inalterado",new Rol(1L,"EMPLEADO"));
        var empleado = new Empleado(); empleado.setActivo(false); empleado.setUnidadVenta(unidad); empleado.setUsuario(cuenta); empleado.setFechaIngreso(LocalDate.of(2020,1,1));
        when(empleados.findById(3L)).thenReturn(Optional.of(empleado));
        var service = new EmpleadoGestionService(empleados,mock(UsuarioRepository.class),mock(RolRepository.class),new BCryptPasswordEncoder(),mock(AsignacionLaboralService.class),mock(EmailService.class),mock(UnidadVentaRepository.class));
        service.reactivar(2L,3L);
        assertTrue(empleado.isActivo()); assertEquals("hash-inalterado",empleado.getUsuario().getPassword());
        assertEquals(LocalDate.of(2020,1,1),empleado.getFechaIngreso());
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.reactivar(99L,3L));
    }

    private Festival festival() {
        var costo = new Costo(); costo.setCostoPorSuperficie(BigDecimal.ZERO); costo.setPlusElectricidad(BigDecimal.ZERO);
        var festival = new Festival(); festival.setId(1L); festival.setFechaInicio(LocalDate.of(2026,1,1)); festival.setFechaFin(LocalDate.of(2026,1,30)); festival.setCosto(costo);
        return festival;
    }
    private FoodTruck unidad(Festival festival) {
        var unidad = new FoodTruck(); unidad.setId(2L); unidad.setSuperficie(20.0); unidad.setFestivales(new ArrayList<>(List.of(festival)));
        return unidad;
    }
}
