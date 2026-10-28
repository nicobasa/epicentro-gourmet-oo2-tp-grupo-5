package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<CierreCaja> findByUnidadIdAndFecha(Long unidadId, LocalDate fecha);
}
