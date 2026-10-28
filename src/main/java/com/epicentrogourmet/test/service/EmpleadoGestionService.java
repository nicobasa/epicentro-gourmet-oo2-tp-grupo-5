package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.EmpleadoRepository;
import com.epicentrogourmet.test.repository.RolRepository;
import com.epicentrogourmet.test.repository.UsuarioRepository;
import com.epicentrogourmet.test.repository.UnidadVentaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmpleadoGestionService {
    private static final char[] PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#".toCharArray();
    private final SecureRandom random = new SecureRandom();
    private final EmpleadoRepository empleados;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final UnidadVentaRepository unidades;
    private final PasswordEncoder encoder;
    private final AsignacionLaboralService asignaciones;
    private final EmailService email;

    public EmpleadoGestionService(EmpleadoRepository empleados, UsuarioRepository usuarios, RolRepository roles,
                                 PasswordEncoder encoder, AsignacionLaboralService asignaciones, EmailService email,
                                 UnidadVentaRepository unidades) {
        this.empleados = empleados;
        this.usuarios = usuarios;
        this.roles = roles;
        this.unidades = unidades;
        this.encoder = encoder;
        this.asignaciones = asignaciones;
        this.email = email;
    }

    @Transactional
    public Empleado alta(Long unidadId, String tipo, String nombre, String apellido, Long dni,
                         LocalDate nacimiento, String emailUsuario, String turno,
                         String especialidad, CategoriaCocina categoria, List<Long> festivalIds) {
        UnidadVenta unidad = unidades.findById(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad de venta inexistente."));
        LocalDate ingreso = LocalDate.now();
        validarDatos(nombre, apellido, dni, nacimiento, ingreso, emailUsuario);
        if (festivalIds == null || festivalIds.isEmpty()) throw new IllegalArgumentException("Seleccioná al menos un festival de trabajo.");

        Empleado existente = empleados.findByDni(dni).orElse(null);
        if (existente != null && (existente.isActivo() || existente.getUnidadVenta() == null
                || !unidadId.equals(existente.getUnidadVenta().getId()))) {
            throw new IllegalArgumentException("El DNI ya pertenece a otro empleado activo o a otra unidad; no se puede transferir desde este flujo.");
        }
        if (existente != null) throw new IllegalArgumentException("Ese empleado está inactivo. Usá el botón Reactivar de la lista de personal inactivo.");
        if (usuarios.findByUsername(emailUsuario.trim()).isPresent())
            throw new IllegalArgumentException("El email ya está asociado a otra cuenta.");

        String password = generarPassword();
        Usuario cuenta = new Usuario();
        cuenta.setRol(roles.findByNombre("EMPLEADO").orElseThrow(() -> new IllegalStateException("Falta el rol EMPLEADO.")));
        cuenta.setUsername(emailUsuario.trim());
        cuenta.setPassword(encoder.encode(password));
        usuarios.save(cuenta);

        Empleado empleado = nuevoEmpleado(tipo, turno, especialidad, categoria);
        empleado.setNombre(nombre.trim());
        empleado.setApellido(apellido.trim());
        empleado.setDni(dni);
        empleado.setFechaNacimiento(nacimiento);
        empleado.setFechaIngreso(ingreso);
        empleado.setSueldoBase(Costo.SUELDO_BASE_PREDETERMINADO.doubleValue());
        empleado.setUnidadVenta(unidad);
        empleado.setUsuario(cuenta);
        empleado.setActivo(true);
        empleados.save(empleado);

        for (Long festivalId : festivalIds.stream().distinct().toList()) {
            Festival festival = unidad.getFestivales().stream().filter(f -> f.getId().equals(festivalId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("La unidad no está habilitada para uno de los festivales seleccionados."));
            LocalDate inicio = ingreso.isAfter(festival.getFechaInicio()) ? ingreso : festival.getFechaInicio();
            if (inicio.isAfter(festival.getFechaFin())) throw new IllegalArgumentException("No se puede dar de alta personal para un festival que ya terminó.");
            asignaciones.asignar(empleado, festival, unidad, inicio, festival.getFechaFin());
        }

        email.enviarCredencialesEmpleado(emailUsuario.trim(), nombre.trim() + " " + apellido.trim(), password);
        return empleado;
    }

    @Transactional
    public Empleado reactivar(Long unidadId, Long empleadoId) {
        Empleado empleado = empleados.findById(empleadoId).orElseThrow(() -> new IllegalArgumentException("Empleado inexistente."));
        exigirPropiedad(empleado, unidadId);
        if (empleado.isActivo()) throw new IllegalArgumentException("El empleado ya está activo.");
        if (empleado.getUsuario() == null || empleado.getUsuario().getRol() == null
                || !"EMPLEADO".equals(empleado.getUsuario().getRol().getNombre()))
            throw new IllegalArgumentException("Solo se puede reactivar personal del staff desde esta pantalla.");
        empleado.setActivo(true);
        return empleados.save(empleado);
    }

    @Transactional
    public Empleado modificar(Long unidadId, Long empleadoId, String nombre, String apellido,
                              LocalDate nacimiento, String emailUsuario, String turno,
                              String especialidad, CategoriaCocina categoria) {
        Empleado empleado = empleados.findById(empleadoId).orElseThrow(() -> new IllegalArgumentException("Empleado inexistente."));
        exigirPropiedad(empleado, unidadId);
        if (!empleado.isActivo()) throw new IllegalArgumentException("No se puede modificar un empleado inactivo.");
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) throw new IllegalArgumentException("Nombre y apellido son obligatorios.");
        if (nacimiento == null || nacimiento.isAfter(LocalDate.now().minusYears(18))) throw new IllegalArgumentException("El empleado debe tener al menos 18 años.");
        if (emailUsuario == null || emailUsuario.isBlank()) throw new IllegalArgumentException("El email es obligatorio.");
        var usuarioExistente = usuarios.findByUsername(emailUsuario.trim());
        if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(empleado.getUsuario().getId())) throw new IllegalArgumentException("El email ya está registrado.");
        empleado.setNombre(nombre.trim());
        empleado.setApellido(apellido.trim());
        empleado.setFechaNacimiento(nacimiento);
        empleado.getUsuario().setUsername(emailUsuario.trim());
        if (empleado instanceof Cajero cajero) cajero.setTurno(turno);
        if (empleado instanceof Cocinero cocinero) {
            if (categoria == null) throw new IllegalArgumentException("La categoría es obligatoria.");
            cocinero.setCategoria(categoria);
            cocinero.setEspecialidadCulinaria(especialidad);
        }
        usuarios.save(empleado.getUsuario());
        return empleados.save(empleado);
    }

    @Transactional
    public void bajaLogica(Long unidadId, Long empleadoId) {
        Empleado empleado = empleados.findById(empleadoId).orElseThrow(() -> new IllegalArgumentException("Empleado inexistente."));
        exigirPropiedad(empleado, unidadId);
        if (empleado.getUsuario() != null && "RESPONSABLE".equals(empleado.getUsuario().getRol().getNombre())) {
            throw new IllegalArgumentException("No se puede dar de baja al Responsable desde la gestión del staff.");
        }
        empleado.setActivo(false);
        empleados.save(empleado);
    }

    private Empleado nuevoEmpleado(String tipo, String turno, String especialidad, CategoriaCocina categoria) {
        if ("CAJERO".equals(tipo)) {
            Cajero c = new Cajero();
            c.setTurno(turno);
            return c;
        }
        if ("COCINERO".equals(tipo)) {
            Cocinero c = new Cocinero();
            if (categoria == null) throw new IllegalArgumentException("Seleccioná una categoría de cocina.");
            c.setCategoria(categoria);
            c.setEspecialidadCulinaria(especialidad == null ? "" : especialidad.trim());
            return c;
        }
        throw new IllegalArgumentException("Tipo de empleado inválido.");
    }

    private static void validarDatos(String nombre, String apellido, Long dni, LocalDate nacimiento,
                                     LocalDate ingreso, String email) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) throw new IllegalArgumentException("Nombre y apellido son obligatorios.");
        if (dni == null || dni < 1000000 || dni > 99999999) throw new IllegalArgumentException("El DNI debe tener entre 7 y 8 dígitos, sin puntos ni letras.");
        if (nacimiento == null || nacimiento.isAfter(LocalDate.now().minusYears(18))) throw new IllegalArgumentException("El empleado debe tener al menos 18 años.");
        if (ingreso == null || ingreso.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de ingreso no puede ser futura.");
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Ingresá un email válido.");
    }

    private static void exigirPropiedad(Empleado empleado, Long unidadId) {
        if (empleado.getUnidadVenta() == null || !unidadId.equals(empleado.getUnidadVenta().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("El empleado no pertenece a tu unidad.");
        }
    }

    private String generarPassword() { char[] value = new char[16]; for (int i = 0; i < value.length; i++) value[i] = PASSWORD_CHARS[random.nextInt(PASSWORD_CHARS.length)]; return new String(value); }
}
