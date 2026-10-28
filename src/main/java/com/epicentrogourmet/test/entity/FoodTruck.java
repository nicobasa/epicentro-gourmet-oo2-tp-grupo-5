package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "food_truck")
@Getter
@Setter
@NoArgsConstructor
public class FoodTruck extends UnidadVenta{

    @NotBlank
    private String patente;
    private boolean requiereElectricidad;
}
