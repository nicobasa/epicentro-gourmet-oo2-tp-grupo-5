
package com.epicentrogourmet.test.entity;

public enum CategoriaCocina {
    COCINERO("Cocinero"),
    COCINERO_AYUDANTE("Cocinero ayudante"),
    LAVAPLATOS("Lavaplatos");

    private final String descripcion;
    CategoriaCocina(String descripcion) { this.descripcion = descripcion; }
    public String getDescripcion() { return descripcion; }
}
