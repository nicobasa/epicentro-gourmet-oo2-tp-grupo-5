package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UnidadVenta u where u.id = :id")
    java.util.Optional<UnidadVenta> bloquearPorId(@org.springframework.data.repository.query.Param("id") Long id);
    List<UnidadVenta> findDistinctByFestivalesId(Long festivalId);
    boolean existsByCodigo(String codigo);
    long countDistinctByPlatosId(Long platoId);
}
