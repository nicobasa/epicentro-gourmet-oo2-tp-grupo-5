package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unidad_venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class UnidadVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombreComercial;

    @Positive
    private Double superficie;

    @NotBlank
    @Pattern(regexp = "[A-Z]{2}[0-9]{8}")
    @Column(unique = true, nullable = false)
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Empleado responsable;

    @OneToMany(mappedBy = "unidadVenta")
    private List<Empleado> empleados = new ArrayList<>();

    @OneToMany(mappedBy = "unidadDeVenta")
    private List<Pedido> pedidos = new ArrayList<>();

    @ManyToMany
    private List<Plato> platos = new ArrayList<>();

    @ManyToMany(mappedBy = "unidadesDeVenta")
    private List<Festival> festivales = new ArrayList<>();
}
