package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "plato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    @NotNull
    @DecimalMin("0.01")
    private Double precioVenta;

    @NotNull
    @DecimalMin("0.00")
    private Double costoProduccion;

    @OneToMany(mappedBy = "plato")
    private List<DetallePedido> detalles;

    @jakarta.validation.constraints.AssertTrue(message = "El costo de producción no puede superar el precio de venta")
    public boolean isMargenValido() { return precioVenta == null || costoProduccion == null || costoProduccion <= precioVenta; }
}
