package com.epicentrogourmet.test.config;

import com.epicentrogourmet.test.entity.Costo;
import com.epicentrogourmet.test.entity.Festival;
import com.epicentrogourmet.test.entity.Rol;
import com.epicentrogourmet.test.entity.Usuario;
import com.epicentrogourmet.test.repository.FestivalRepository;
import com.epicentrogourmet.test.repository.RolRepository;
import com.epicentrogourmet.test.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner cargarDatosIniciales(RolRepository roles, FestivalRepository festivales,
                                           UsuarioRepository usuarios, PasswordEncoder encoder,
                                           com.epicentrogourmet.test.service.HistorialService historial,
                                           @Value("${app.bootstrap.admin-username:}") String adminUsername,
                                           @Value("${app.bootstrap.admin-password:}") String adminPassword) {
        return args -> {
            Rol admin = rol(roles, "ADMIN");
            rol(roles, "RESPONSABLE");
            rol(roles, "EMPLEADO");

            if (adminUsername != null && !adminUsername.isBlank() && adminPassword != null && !adminPassword.isBlank()
                    && usuarios.findByUsername(adminUsername).isEmpty()) {
                Usuario cuenta = new Usuario();
                cuenta.setUsername(adminUsername.trim());
                cuenta.setPassword(encoder.encode(adminPassword));
                cuenta.setRol(admin);
                usuarios.save(cuenta);
            }

            // Demo event data is inserted only in an empty festival table and never replaces existing records.
            if (festivales.count() == 0) {
                festivales.saveAll(List.of(
                        festival("Festival de la Primavera", "Primavera", LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 28)),
                        festival("Festival del Verano", "Verano", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 30)),
                        festival("Festival Sabores de Buenos Aires", "Otoño", LocalDate.of(2027, 3, 1), LocalDate.of(2027, 4, 10))));
            }

            historial.conservarValoresExistentes();
        };
    }

    private static Rol rol(RolRepository roles, String nombre) {
        return roles.findByNombre(nombre).orElseGet(() -> roles.save(new Rol(null, nombre)));
    }

    private static Festival festival(String nombre, String temporada, LocalDate inicio, LocalDate fin) {
        Costo costo = Costo.valoresPredeterminados();
        Festival festival = new Festival();
        festival.setNombre(nombre);
        festival.setTemporada(temporada);
        festival.setFechaInicio(inicio);
        festival.setFechaFin(fin);
        festival.setCosto(costo);
        return festival;
    }
}
