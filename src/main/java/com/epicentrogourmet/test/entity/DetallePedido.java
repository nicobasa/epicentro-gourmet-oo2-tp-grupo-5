package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cantidad;
    private Double precioUnitario;
    private Double costoUnitario;

    @PrePersist
    public void conservarCostoDeVenta() {
        if (costoUnitario == null && plato != null) costoUnitario = plato.getCostoProduccion();
    }

    @ManyToOne
    @JoinColumn(name = "plato_id")
    private Plato plato;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
}
