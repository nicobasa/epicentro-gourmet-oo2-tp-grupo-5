package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cocinero")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cocinero extends Empleado {

    private String especialidadCulinaria;
    private Double plusCategoria;
    private CategoriaCocina categoria;
}