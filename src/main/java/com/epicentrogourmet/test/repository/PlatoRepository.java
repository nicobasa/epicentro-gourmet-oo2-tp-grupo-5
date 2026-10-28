package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatoRepository extends JpaRepository<Plato, Long> {
}
