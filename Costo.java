
package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "costo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Costo {

    public static final BigDecimal COSTO_POR_SUPERFICIE_PREDETERMINADO = new BigDecimal("500.00");
    public static final BigDecimal COSTO_POR_MONTAJE_PREDETERMINADO = new BigDecimal("10.00");
    public static final BigDecimal PLUS_ELECTRICIDAD_PREDETERMINADO = new BigDecimal("2000.00");
    public static final BigDecimal SUELDO_BASE_PREDETERMINADO = new BigDecimal("100000.00");
    public static final BigDecimal MONTO_ANIO_ANTIGUEDAD_PREDETERMINADO = new BigDecimal("5000.00");
    public static final BigDecimal PLUS_COCINERO_PREDETERMINADO = new BigDecimal("150000.00");
    public static final BigDecimal PLUS_COCINERO_AYUDANTE_PREDETERMINADO = new BigDecimal("100000.00");
    public static final BigDecimal PLUS_LAVAPLATOS_PREDETERMINADO = new BigDecimal("50000.00");

    /** Valores iniciales del enunciado; cada festival conserva después sus costos configurados. */
    public static Costo valoresPredeterminados() {
        return new Costo(null, COSTO_POR_SUPERFICIE_PREDETERMINADO, COSTO_POR_MONTAJE_PREDETERMINADO,
                PLUS_ELECTRICIDAD_PREDETERMINADO, SUELDO_BASE_PREDETERMINADO,
                MONTO_ANIO_ANTIGUEDAD_PREDETERMINADO, PLUS_COCINERO_PREDETERMINADO,
                PLUS_COCINERO_AYUDANTE_PREDETERMINADO, PLUS_LAVAPLATOS_PREDETERMINADO);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoPorSuperficie;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoPorMontaje;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal plusElectricidad;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sueldoBase;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAnioAntiguedad;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal plusCocinero;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal plusCocineroAyudante;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "Los parámetros económicos no pueden ser negativos.")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal plusLavaplatos;
}
