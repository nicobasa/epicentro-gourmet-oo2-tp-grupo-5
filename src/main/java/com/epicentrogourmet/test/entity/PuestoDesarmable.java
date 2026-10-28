package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "puesto_desarmable")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PuestoDesarmable extends UnidadVenta {

    @Positive
    private Integer cantidadCarpas;
    @Positive
    private Integer tiempoMontaje;
}
