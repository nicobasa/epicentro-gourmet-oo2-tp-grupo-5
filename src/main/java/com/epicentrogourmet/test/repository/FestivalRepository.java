package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
}
