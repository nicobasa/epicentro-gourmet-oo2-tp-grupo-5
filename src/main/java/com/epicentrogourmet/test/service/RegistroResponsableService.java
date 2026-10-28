package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RegistroResponsableService {
    private final FestivalRepository festivales;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final UnidadVentaRepository unidades;
    private final EmpleadoRepository empleados;
    private final PasswordEncoder passwordEncoder;
    private final ReporteService reportes;

    public RegistroResponsableService(FestivalRepository festivales, UsuarioRepository usuarios, RolRepository roles,
                                      UnidadVentaRepository unidades, EmpleadoRepository empleados,
                                      PasswordEncoder passwordEncoder, ReporteService reportes) {
        this.festivales = festivales;
        this.usuarios = usuarios;
        this.roles = roles;
        this.unidades = unidades;
        this.empleados = empleados;
        this.passwordEncoder = passwordEncoder;
        this.reportes = reportes;
    }

    @Transactional
    public Empleado registrar(Long festivalId, String tipo, String nombreComercial, Double superficie, String codigo,
                              String patente, boolean requiereElectricidad, Integer cantidadCarpas, Integer tiempoMontaje,
                              String nombre, String apellido, Long dni, LocalDate nacimiento, String username, String password) {
        Festival festival = festivales.findById(festivalId).orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));
        if (isBlank(nombreComercial) || superficie == null || !Double.isFinite(superficie) || superficie <= 0 || codigo == null || !codigo.matches("[A-Z]{2}[0-9]{8}")) {
            throw new IllegalArgumentException("Revisá el nombre, la superficie y el código de la unidad.");
        }
        if (unidades.existsByCodigo(codigo)) throw new IllegalArgumentException("El código de unidad ya está registrado.");
        if (dni == null || dni < 1000000 || dni > 99999999) throw new IllegalArgumentException("El DNI del Responsable debe tener entre 7 y 8 dígitos, sin puntos ni letras.");
        if (isBlank(nombre) || isBlank(apellido)) throw new IllegalArgumentException("El nombre y apellido del Responsable son obligatorios.");
        if (empleados.findByDni(dni).isPresent()) throw new IllegalArgumentException("El DNI del Responsable ya está registrado.");
        validarMayorDeEdad(nacimiento);
        if (isBlank(username) || isBlank(password) || usuarios.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("El usuario es obligatorio y debe ser único.");
        }

        Usuario cuenta = new Usuario();
        cuenta.setUsername(username.trim());
        cuenta.setPassword(passwordEncoder.encode(password));
        cuenta.setRol(roles.findByNombre("RESPONSABLE").orElseThrow(() -> new IllegalStateException("Falta el rol RESPONSABLE.")));
        usuarios.save(cuenta);

        UnidadVenta unidad;
        if ("FOOD_TRUCK".equals(tipo)) {
            if (isBlank(patente)) throw new IllegalArgumentException("La patente es obligatoria para un Food Truck.");
            FoodTruck truck = new FoodTruck();
            truck.setPatente(patente.trim());
            truck.setRequiereElectricidad(requiereElectricidad);
            unidad = truck;
        } else if ("PUESTO_DESARMABLE".equals(tipo)) {
            if (cantidadCarpas == null || cantidadCarpas < 1 || tiempoMontaje == null || tiempoMontaje < 1) {
                throw new IllegalArgumentException("Carpas y tiempo de montaje deben ser mayores que cero.");
            }
            PuestoDesarmable puesto = new PuestoDesarmable();
            puesto.setCantidadCarpas(cantidadCarpas);
            puesto.setTiempoMontaje(tiempoMontaje);
            unidad = puesto;
        } else throw new IllegalArgumentException("Tipo de unidad inválido.");

        unidad.setNombreComercial(nombreComercial.trim());
        unidad.setSuperficie(superficie);
        unidad.setCodigo(codigo);
        unidades.save(unidad);

        Empleado responsable = new Empleado();
        responsable.setNombre(nombre.trim());
        responsable.setApellido(apellido.trim());
        responsable.setDni(dni);
        responsable.setFechaNacimiento(nacimiento);
        responsable.setFechaIngreso(LocalDate.now());
        responsable.setSueldoBase(Costo.SUELDO_BASE_PREDETERMINADO.doubleValue());
        responsable.setActivo(true);
        responsable.setUnidadVenta(unidad);
        responsable.setUsuario(cuenta);
        empleados.save(responsable);

        unidad.setResponsable(responsable);
        festival.getUnidadesDeVenta().add(unidad);
        festivales.save(festival);
        reportes.conservarCanon(unidad, festival);
        return responsable;
    }

    private static void validarMayorDeEdad(LocalDate nacimiento) {
        if (nacimiento == null || nacimiento.isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("El responsable debe tener al menos 18 años.");
        }
    }

    private static boolean isBlank(String value) { return value == null || value.isBlank(); }
}
