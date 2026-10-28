package com.epicentrogourmet.test.dto;

/**
 * Plato Estrella: cantidad de pedidos distintos que contienen el plato.
 * Se conserva el nombre cantidadVendida del componente para mantener la compatibilidad del DTO.
 */
public record PlatoDTO(Long platoId, String nombre, Long cantidadVendida) { }
