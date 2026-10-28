package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cierre_caja", uniqueConstraints = @UniqueConstraint(
        name = "uk_cierre_caja_unidad_fecha", columnNames = {"unidad_venta_id", "fecha"}))
@Getter
@Setter
@NoArgsConstructor
public class CierreCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidad;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cajero_id", nullable = false)
    private Cajero cajero;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalDateTime cerradoEn;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;
}
