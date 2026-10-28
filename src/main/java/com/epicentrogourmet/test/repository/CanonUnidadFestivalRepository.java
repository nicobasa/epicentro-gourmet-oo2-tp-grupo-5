package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.CanonUnidadFestival;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CanonUnidadFestivalRepository extends JpaRepository<CanonUnidadFestival, Long> {
    Optional<CanonUnidadFestival> findByFestivalIdAndUnidadId(Long festivalId, Long unidadId);
    void deleteByFestivalId(Long festivalId);
}
