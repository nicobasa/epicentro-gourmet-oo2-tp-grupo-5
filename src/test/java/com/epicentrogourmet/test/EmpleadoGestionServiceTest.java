package com.epicentrogourmet.test;

import com.epicentrogourmet.test.entity.Empleado;
import com.epicentrogourmet.test.entity.UnidadVenta;
import com.epicentrogourmet.test.repository.EmpleadoRepository;
import com.epicentrogourmet.test.repository.RolRepository;
import com.epicentrogourmet.test.repository.UnidadVentaRepository;
import com.epicentrogourmet.test.repository.UsuarioRepository;
import com.epicentrogourmet.test.service.AsignacionLaboralService;
import com.epicentrogourmet.test.service.EmailService;
import com.epicentrogourmet.test.service.EmpleadoGestionService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmpleadoGestionServiceTest {
    @Test
    void responsableNoPuedeDarDeBajaEmpleadoDeOtraUnidad() {
        EmpleadoRepository empleados = mock(EmpleadoRepository.class);
        Empleado empleado = new Empleado(); empleado.setId(30L);
        UnidadVenta ajena = new UnidadVenta(); ajena.setId(2L); empleado.setUnidadVenta(ajena);
        when(empleados.findById(30L)).thenReturn(Optional.of(empleado));
        EmpleadoGestionService service = new EmpleadoGestionService(empleados, mock(UsuarioRepository.class),
                mock(RolRepository.class), mock(PasswordEncoder.class), mock(AsignacionLaboralService.class),
                mock(EmailService.class), mock(UnidadVentaRepository.class));
        assertThrows(AccessDeniedException.class, () -> service.bajaLogica(1L, 30L));
        verify(empleados, never()).save(any());
    }

    @Test
    void noReactivaPorDniAEmpleadoDeOtraUnidad() {
        EmpleadoRepository empleados = mock(EmpleadoRepository.class);
        UnidadVenta unidadPropia = new UnidadVenta(); unidadPropia.setId(1L);
        UnidadVenta ajena = new UnidadVenta(); ajena.setId(2L);
        Empleado inactivo = new Empleado(); inactivo.setId(40L); inactivo.setDni(32123456L); inactivo.setActivo(false); inactivo.setUnidadVenta(ajena);
        when(empleados.findByDni(32123456L)).thenReturn(Optional.of(inactivo));
        UnidadVentaRepository unidades = mock(UnidadVentaRepository.class);
        when(unidades.findById(1L)).thenReturn(Optional.of(unidadPropia));
        EmpleadoGestionService service = new EmpleadoGestionService(empleados, mock(UsuarioRepository.class),
                mock(RolRepository.class), mock(PasswordEncoder.class), mock(AsignacionLaboralService.class),
                mock(EmailService.class), unidades);
        assertThrows(IllegalArgumentException.class, () -> service.alta(1L, "CAJERO", "Nombre", "Apellido", 32123456L,
                java.time.LocalDate.of(1990, 1, 1), "a@example.test", "Manana",
                null, null, java.util.List.of(7L)));
        verify(empleados, never()).save(any());
    }
}
