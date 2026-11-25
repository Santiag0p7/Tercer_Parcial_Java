package com.caketrack.repository;

import com.caketrack.model.DetallePedido;
import com.caketrack.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedido(Pedido pedido);
}
