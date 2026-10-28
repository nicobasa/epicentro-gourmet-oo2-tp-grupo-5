package com.epicentrogourmet.test.service;

import com.epicentrogourmet.test.entity.Festival;
import com.epicentrogourmet.test.repository.FestivalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FestivalService {

    private final FestivalRepository repository;
    private final com.epicentrogourmet.test.repository.UnidadVentaRepository unidades;
    private final com.epicentrogourmet.test.repository.PedidoRepository pedidos;
    private final com.epicentrogourmet.test.repository.AsignacionLaboralRepository asignaciones;
    private final com.epicentrogourmet.test.repository.CanonUnidadFestivalRepository canones;
    private final HistorialService historial;
    private final ReporteService reportes;
    private final jakarta.validation.Validator validator;

    public FestivalService(FestivalRepository repository,
            com.epicentrogourmet.test.repository.UnidadVentaRepository unidades,
            com.epicentrogourmet.test.repository.PedidoRepository pedidos,
            com.epicentrogourmet.test.repository.AsignacionLaboralRepository asignaciones,
            com.epicentrogourmet.test.repository.CanonUnidadFestivalRepository canones,
            HistorialService historial, ReporteService reportes, jakarta.validation.Validator validator) {
        this.repository = repository;
        this.unidades = unidades; this.pedidos = pedidos; this.asignaciones = asignaciones;
        this.canones = canones; this.historial = historial; this.reportes = reportes; this.validator = validator;
    }

    public Festival agregar(Festival objeto) {
        return repository.save(objeto);
    }

    public void actualizar(Festival objeto) {
        repository.save(objeto);
    }

    public void eliminar(Festival objeto) {
        repository.delete(objeto);
    }

    public Festival traer(Long idFestival) {
        return repository.findById(idFestival).orElse(null);
    }

    public List<Festival> traer() {
        return repository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public Festival crear(com.epicentrogourmet.test.dto.FestivalForm datos) {
        validar(datos);
        Festival festival = new Festival();
        aplicar(festival, datos);
        return repository.save(festival);
    }

    @org.springframework.transaction.annotation.Transactional
    public void modificar(Long id, com.epicentrogourmet.test.dto.FestivalForm datos) {
        validar(datos);
        Festival festival = requerir(id);
        if ((pedidos.existsByFestivalId(id) || asignaciones.existsByFestivalId(id))
                && (!datos.getFechaInicio().equals(festival.getFechaInicio()) || !datos.getFechaFin().equals(festival.getFechaFin())))
            throw new IllegalArgumentException("No se pueden cambiar las fechas de un festival con pedidos o asignaciones históricas.");
        historial.conservarValoresExistentes();
        aplicar(festival, datos);
        repository.save(festival);
    }

    @org.springframework.transaction.annotation.Transactional
    public void vincularUnidad(Long festivalId, Long unidadId) {
        var unidad = unidades.bloquearPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        Festival festival = requerir(festivalId);
        if (festival.getUnidadesDeVenta().stream().noneMatch(u -> u.getId().equals(unidadId))) {
            festival.getUnidadesDeVenta().add(unidad);
            repository.save(festival);
        }
        reportes.conservarCanon(unidad, festival);
    }

    @org.springframework.transaction.annotation.Transactional
    public void quitarUnidad(Long festivalId, Long unidadId) {
        unidades.bloquearPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inexistente."));
        Festival festival = requerir(festivalId);
        if (pedidos.existsByFestivalIdAndUnidadDeVentaId(festivalId, unidadId))
            throw new IllegalArgumentException("No se puede desvincular esta unidad: tiene pedidos históricos en el festival. Se conserva la relación para mantener las recaudaciones y los reportes.");
        if (asignaciones.existsByUnidadIdAndFestivalId(unidadId, festivalId))
            throw new IllegalArgumentException("No se puede desvincular esta unidad: tiene asignaciones laborales en el festival que deben conservarse.");
        festival.getUnidadesDeVenta().removeIf(u -> u.getId().equals(unidadId));
        repository.save(festival);
    }

    @org.springframework.transaction.annotation.Transactional
    public void eliminarSinHistorial(Long id) {
        Festival festival = requerir(id);
        if (pedidos.existsByFestivalId(id) || asignaciones.existsByFestivalId(id))
            throw new IllegalArgumentException("No se puede eliminar un festival que registra pedidos o asignaciones históricas.");
        canones.deleteByFestivalId(id);
        festival.getUnidadesDeVenta().clear();
        repository.delete(festival);
    }

    private Festival requerir(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Festival inexistente."));
    }

    private void validar(com.epicentrogourmet.test.dto.FestivalForm datos) {
        var errores = validator.validate(datos);
        if (!errores.isEmpty()) throw new IllegalArgumentException(errores.stream()
                .map(e -> e.getPropertyPath() + ": " + e.getMessage()).sorted().collect(java.util.stream.Collectors.joining(" ")));
    }

    private static void aplicar(Festival festival, com.epicentrogourmet.test.dto.FestivalForm datos) {
        var costo = festival.getCosto() == null ? new com.epicentrogourmet.test.entity.Costo() : festival.getCosto();
        costo.setCostoPorSuperficie(datos.getCostoPorSuperficie()); costo.setCostoPorMontaje(datos.getCostoPorMontaje());
        costo.setPlusElectricidad(datos.getPlusElectricidad()); costo.setSueldoBase(datos.getSueldoBase());
        costo.setMontoAnioAntiguedad(datos.getMontoAnioAntiguedad()); costo.setPlusCocinero(datos.getPlusCocinero());
        costo.setPlusCocineroAyudante(datos.getPlusCocineroAyudante()); costo.setPlusLavaplatos(datos.getPlusLavaplatos());
        festival.setNombre(datos.getNombre().trim()); festival.setTemporada(datos.getTemporada().trim());
        festival.setFechaInicio(datos.getFechaInicio()); festival.setFechaFin(datos.getFechaFin()); festival.setCosto(costo);
    }
}
