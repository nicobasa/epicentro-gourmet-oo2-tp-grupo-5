package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Records the employee's participation in one specific unit and festival. */
@Entity
@Table(name = "asignacion_laboral", uniqueConstraints = @UniqueConstraint(
        name = "uk_asignacion_empleado_festival_unidad",
        columnNames = {"empleado_id", "festival_id", "unidad_venta_id", "fecha_inicio"}))
@Getter
@Setter
@NoArgsConstructor
public class AsignacionLaboral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidad;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(precision = 14, scale = 2)
    private java.math.BigDecimal sueldoLiquidado;
}
