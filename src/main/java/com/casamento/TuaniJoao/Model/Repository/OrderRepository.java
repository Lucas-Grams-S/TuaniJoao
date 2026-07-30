package com.casamento.TuaniJoao.Model.Repository;

import com.casamento.TuaniJoao.Model.Entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Busca um pedido pelo ID de transação do Mercado Pago
    Optional<Order> findByMpPaymentId(String mpPaymentId);

    // Lista todos os pedidos ordenados pelos mais recentes primeiro
    List<Order> findAllByOrderByCreatedAtDesc();

    // Lista pedidos por status (ex: buscar apenas os APROVADOS)
    List<Order> findByStatusOrderByCreatedAtDesc(String status);
}