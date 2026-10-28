package com.epicentrogourmet.test.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "festival")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;
    @NotBlank
    private String temporada;
    @NotNull
    private LocalDate fechaInicio;
    @NotNull
    private LocalDate fechaFin;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "costo_id")
    private Costo costo;

    @ManyToMany
    private List<UnidadVenta> unidadesDeVenta = new ArrayList<>();

    @OneToMany(mappedBy = "festival")
    private List<Pedido> pedidos = new ArrayList<>();

    public String getTemporada() { return "Otono".equalsIgnoreCase(temporada) ? "Otoño" : temporada; }

    public void setFechaFin(LocalDate fechaFin) {
        if (fechaFin != null
                && fechaInicio != null
                && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        this.fechaFin = fechaFin;
    }

    @jakarta.validation.constraints.AssertTrue(message = "La fecha de fin debe ser igual o posterior a la fecha de inicio")
    public boolean isPeriodoValido() {
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }
}
