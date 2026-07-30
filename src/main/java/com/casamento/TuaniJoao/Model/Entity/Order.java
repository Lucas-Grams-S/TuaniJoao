package com.casamento.TuaniJoao.Model.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders") // "order" é palavra reservada no SQL, por isso usamos "orders"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificador único da transação devolvido pelo Mercado Pago
    @Column(name = "mp_payment_id", unique = true)
    private String mpPaymentId;

    // Dados do Convidado
    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String guestEmail;

    @Column(nullable = false)
    private String guestCpf;

    @Column(length = 500)
    private String message;

    // Dados do Presente
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gift_id", nullable = false)
    private Gift gift;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod; // "PIX" ou "CREDIT_CARD"

    // PENDENTE, APROVADO, RECUSADO, CANCELADO
    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDENTE";
        }
    }
}