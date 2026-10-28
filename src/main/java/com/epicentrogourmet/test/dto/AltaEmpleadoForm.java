package com.epicentrogourmet.test.dto;

import com.epicentrogourmet.test.entity.CategoriaCocina;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AltaEmpleadoForm {
    @NotNull(message = "Seleccioná una unidad de venta.")
    private Long unidadId;
    @Pattern(regexp = "CAJERO|COCINERO", message = "Seleccioná un puesto válido.")
    private String tipoEmpleado = "CAJERO";
    @NotBlank(message = "Ingresá el nombre.") @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
    private String nombre;
    @NotBlank(message = "Ingresá el apellido.") @Size(max = 100, message = "El apellido admite hasta 100 caracteres.")
    private String apellido;
    @NotBlank(message = "Ingresá el DNI.")
    @Pattern(regexp = "[0-9]{7,8}", message = "El DNI debe tener entre 7 y 8 dígitos, sin puntos ni letras.")
    private String dni;
    @NotNull(message = "Ingresá la fecha de nacimiento.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;
    @NotBlank(message = "Ingresá el email de acceso.") @Email(message = "Ingresá un email válido.")
    private String emailUsuario;
    private String turno = "Mañana";
    private String especialidadCulinaria;
    private CategoriaCocina categoriaCocina = CategoriaCocina.COCINERO;
    @NotEmpty(message = "Seleccioná al menos un festival de trabajo.")
    private List<Long> festivalIds = new ArrayList<>();
}
