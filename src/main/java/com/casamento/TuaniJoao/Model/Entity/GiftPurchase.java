package com.casamento.TuaniJoao.Model.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "gift_purchases")
public class GiftPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Qual presente foi escolhido?
    @ManyToOne
    @JoinColumn(name = "gift_id", nullable = false)
    private Gift gift;

    // Quem está dando o presente?
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    // Valor pago (importante salvar, caso o preço do presente mude no catálogo depois)
    @Column(nullable = false)
    private BigDecimal amountPaid;

    // Status do pagamento: "PENDING" (Aguardando Pix), "PAID" (Pago), "CANCELLED"
    @Column(nullable = false)
    private String paymentStatus = "PENDING";

    // ID gerado pelo gateway de pagamento para batermos a conciliação depois
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    // Uma mensagem fofa que o convidado pode deixar para vocês na hora da compra!
    @Column(length = 1000)
    private String messageToCouple;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

}
