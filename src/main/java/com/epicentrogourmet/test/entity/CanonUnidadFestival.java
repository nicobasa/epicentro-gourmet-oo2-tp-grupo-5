package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

/** Importe acordado al vincular una unidad a un festival; conserva el canon histórico. */
@Entity
@Table(name = "canon_unidad_festival", uniqueConstraints = @UniqueConstraint(
        name = "uk_canon_festival_unidad", columnNames = {"festival_id", "unidad_venta_id"}))
@Getter @Setter @NoArgsConstructor
public class CanonUnidadFestival {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidad;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;
}
