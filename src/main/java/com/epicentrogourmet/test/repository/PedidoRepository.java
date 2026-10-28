package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    boolean existsByFestivalIdAndUnidadDeVentaId(Long festivalId, Long unidadId);
    boolean existsByFestivalId(Long festivalId);
    List<Pedido> findByUnidadDeVentaIdAndFechaTransaccionBetween(Long unidadId, LocalDateTime desde, LocalDateTime hasta);
    List<Pedido> findByFestivalId(Long festivalId);
    List<Pedido> findByFestivalIdAndUnidadDeVentaId(Long festivalId, Long unidadId);
    List<Pedido> findByUnidadDeVentaIdOrderByFechaTransaccionDesc(Long unidadId);
}
