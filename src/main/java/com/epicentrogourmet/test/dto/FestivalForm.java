package com.epicentrogourmet.test.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class FestivalForm {
    @NotBlank(message = "El nombre del festival es obligatorio.")
    @Size(max = 255, message = "El nombre admite hasta 255 caracteres.")
    private String nombre;
    @NotBlank(message = "La temporada es obligatoria.")
    @Size(max = 255, message = "La temporada admite hasta 255 caracteres.")
    private String temporada;
    @NotNull(message = "Ingresá la fecha de inicio.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;
    @NotNull(message = "Ingresá la fecha de fin.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El costo por superficie no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal costoPorSuperficie;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El costo por montaje no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal costoPorMontaje;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El plus de electricidad no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal plusElectricidad;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El sueldo base no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal sueldoBase;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El adicional por antigüedad no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal montoAnioAntiguedad;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El plus de cocinero no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal plusCocinero;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El plus de ayudante no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal plusCocineroAyudante;
    @NotNull(message = "Todos los importes son obligatorios.") @DecimalMin(value = "0", message = "El plus de lavaplatos no puede ser negativo.") @Digits(integer = 10, fraction = 2, message = "El importe admite hasta 10 dígitos enteros y 2 decimales.")
    private BigDecimal plusLavaplatos;
    @AssertTrue(message = "La fecha de fin no puede ser anterior a la fecha de inicio.")
    public boolean isPeriodoValido() { return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio); }
}
