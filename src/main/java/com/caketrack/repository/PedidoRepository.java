package com.caketrack.repository;

import com.caketrack.model.EstadoPedido;
import com.caketrack.model.Pedido;
import com.caketrack.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteOrderByFechaEntregaAsc(Usuario cliente);

    List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);

    List<Pedido> findByEstadoInOrderByFechaEntregaAsc(Collection<EstadoPedido> estados);
}
