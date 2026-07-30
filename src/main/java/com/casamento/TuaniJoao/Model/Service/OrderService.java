package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Exception.ResourceNotFoundException;
import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Entity.Order;
import com.casamento.TuaniJoao.Model.Repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Cria e salva um pedido inicial logo após a resposta do Mercado Pago.
     */
    @Transactional
    public Order createOrder(Gift gift, String guestName, String guestEmail, String guestCpf,
                             String message, BigDecimal amount, String paymentMethod,
                             String mpPaymentId, String statusInitial) {

        log.info("Registrando novo pedido para o presente '{}' | Convidado: {}", gift.getName(), guestName);

        Order order = new Order();
        order.setGift(gift);
        order.setGuestName(guestName);
        order.setGuestEmail(guestEmail);
        order.setGuestCpf(guestCpf);
        order.setMessage(message);
        order.setAmount(amount);
        order.setPaymentMethod(paymentMethod);
        order.setMpPaymentId(mpPaymentId);
        order.setStatus(statusInitial);

        return orderRepository.save(order);
    }

    /**
     * Atualiza o status de um pedido existente a partir do ID do Mercado Pago.
     */
    @Transactional
    public Order updateOrderStatusByMpId(String mpPaymentId, String newStatus) {
        log.info("Atualizando status do pedido MP ID '{}' para '{}'", mpPaymentId, newStatus);

        Order order = orderRepository.findByMpPaymentId(mpPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para o ID MP: " + mpPaymentId));

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Lista todos os pedidos para exibição no painel administrativo dos noivos.
     */
    public List<Order> findAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }
}