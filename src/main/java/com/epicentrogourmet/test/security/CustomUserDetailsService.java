package com.epicentrogourmet.test.security;

import com.epicentrogourmet.test.entity.Usuario;
import com.epicentrogourmet.test.service.UsuarioService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.epicentrogourmet.test.service.EmpleadoService;
import com.epicentrogourmet.test.entity.Empleado;
import com.epicentrogourmet.test.entity.Cajero;

import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioService usuarioService;
    private final EmpleadoService empleadoService;

    public CustomUserDetailsService(UsuarioService usuarioService, EmpleadoService empleadoService) {
        this.usuarioService = usuarioService;
        this.empleadoService = empleadoService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioService.traerPorUsername(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        Empleado empleado = empleadoService.traerPorUsername(username);
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));
        if (empleado instanceof Cajero && "EMPLEADO".equals(usuario.getRol().getNombre())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CAJERO"));
        }
        boolean cuentaActiva = "ADMIN".equals(usuario.getRol().getNombre())
                || (empleado != null && empleado.isActivo());

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .disabled(!cuentaActiva)
                .build();
    }
}
