
package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "empleado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    @Positive
    private Long dni;
    @NotNull
    @Past
    private LocalDate fechaNacimiento;
    @NotNull
    private LocalDate fechaIngreso;
    private Double sueldoBase;
    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "unidad_venta_id")
    private UnidadVenta unidadVenta;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    public void setFechaNacimiento(LocalDate fechaNacimiento) {

        if (fechaNacimiento == null) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser nula");
        }

        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException(
                    "El empleado debe ser mayor de edad");
        }

        this.fechaNacimiento = fechaNacimiento;
    }

    public int calcularAntiguedad() {

        if (fechaIngreso == null) {
            return 0;
        }

        return Period.between(
                fechaIngreso,
                LocalDate.now()
        ).getYears();
    }

    @Transient
    public String getPuestoDescripcion() {
        if (this instanceof Cajero) return "Cajero";
        if (this instanceof Cocinero cocinero)
            return cocinero.getCategoria() == null ? "Cocina" : cocinero.getCategoria().getDescripcion();
        return usuario != null && usuario.getRol() != null && "RESPONSABLE".equals(usuario.getRol().getNombre())
                ? "Responsable" : "Empleado";
    }
}
