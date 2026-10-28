package com.epicentrogourmet.test.repository;

import com.epicentrogourmet.test.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    boolean existsByPlatoId(Long platoId);
    java.util.List<DetallePedido> findByPedidoId(Long pedidoId);
}
