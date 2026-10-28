package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByUsuarioUsername(String username);

    Optional<Empleado> findByDni(Long dni);

    List<Empleado> findByUnidadVentaIdAndActivoTrue(Long unidadVentaId);
    List<Empleado> findByUnidadVentaIdAndActivoFalse(Long unidadVentaId);
}
