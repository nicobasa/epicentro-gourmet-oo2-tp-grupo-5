package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.AsignacionLaboral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionLaboralRepository extends JpaRepository<AsignacionLaboral, Long> {
    boolean existsByUnidadIdAndFestivalId(Long unidadId, Long festivalId);
    boolean existsByFestivalId(Long festivalId);
    List<AsignacionLaboral> findByEmpleadoIdAndEmpleadoActivoTrue(Long empleadoId);
    List<AsignacionLaboral> findByEmpleadoUnidadVentaIdAndFestivalId(Long unidadId, Long festivalId);
    List<AsignacionLaboral> findByUnidadIdAndFestivalId(Long unidadId, Long festivalId);
    List<AsignacionLaboral> findByEmpleadoIdAndEmpleadoActivoTrueAndFestivalId(Long empleadoId, Long festivalId);
    boolean existsByEmpleadoIdAndFestivalIdAndUnidadId(Long empleadoId, Long festivalId, Long unidadId);
    Optional<AsignacionLaboral> findByEmpleadoIdAndFestivalIdAndUnidadId(Long empleadoId, Long festivalId, Long unidadId);
    Optional<AsignacionLaboral> findByEmpleadoIdAndFestivalIdAndUnidadIdAndFechaInicio(Long empleadoId, Long festivalId, Long unidadId, java.time.LocalDate fechaInicio);
}
