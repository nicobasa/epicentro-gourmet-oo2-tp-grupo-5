package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cajero")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cajero extends Empleado {

    private String turno;

    public String getTurno() { return "Manana".equalsIgnoreCase(turno) ? "Mañana" : turno; }

    public void setTurno(String turno) {

        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }

        if (turno.equalsIgnoreCase("Manana")
                || turno.equalsIgnoreCase("Mañana")) {

            this.turno = "Mañana";

        } else if (turno.equalsIgnoreCase("Noche")) {

            this.turno = "Noche";

        } else {

            throw new IllegalArgumentException(
                    "El turno del cajero debe ser Mañana o Noche");
        }
    }
}