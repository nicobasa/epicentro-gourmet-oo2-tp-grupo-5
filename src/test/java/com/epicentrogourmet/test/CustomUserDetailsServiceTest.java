package com.epicentrogourmet.test;

import com.epicentrogourmet.test.entity.Empleado;
import com.epicentrogourmet.test.entity.Rol;
import com.epicentrogourmet.test.entity.Usuario;
import com.epicentrogourmet.test.service.EmpleadoService;
import com.epicentrogourmet.test.service.UsuarioService;
import com.epicentrogourmet.test.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {
    @Test
    void cuentaDeEmpleadoInactivoNoSePuedeAutenticar() {
        UsuarioService usuarios = mock(UsuarioService.class);
        EmpleadoService empleados = mock(EmpleadoService.class);
        Rol rol = new Rol(1L, "EMPLEADO");
        Usuario user = new Usuario(1L, "staff@example.test", "{bcrypt}hash", rol);
        Empleado empleado = new Empleado(); empleado.setActivo(false);
        when(usuarios.traerPorUsername(user.getUsername())).thenReturn(user);
        when(empleados.traerPorUsername(user.getUsername())).thenReturn(empleado);
        var details = new CustomUserDetailsService(usuarios, empleados).loadUserByUsername(user.getUsername());
        assertFalse(details.isEnabled());
    }
}
