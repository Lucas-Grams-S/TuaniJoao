package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Dto.CardPaymentDTO;
import com.casamento.TuaniJoao.Model.Dto.GuestPaymentDTO;
import com.casamento.TuaniJoao.Model.Dto.PixResponseDTO;
import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import com.casamento.TuaniJoao.Model.Service.MercadoPagoService;
import com.casamento.TuaniJoao.Model.Service.OrderService;
import com.mercadopago.resources.payment.Payment;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final GiftService giftService;
    private final MercadoPagoService mercadoPagoService;
    private final OrderService orderService;

    @PostMapping("/pix/{giftId}")
    public ResponseEntity<?> criarPagamentoPix(@PathVariable Long giftId, @RequestBody GuestPaymentDTO payerInfo) {
        try {
            log.info("🎯 Requisição recebida para Pix ID {}. Pagador: {}", giftId, payerInfo.getName());
            Gift gift = giftService.findById(giftId);
            if (gift == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Presente não encontrado no catálogo."));
            }

            Payment payment = mercadoPagoService.gerarPagamentoPix(gift.getName(), gift.getPrice(), payerInfo);

            Long paymentId = payment.getId();
            String status = payment.getStatus();
            String copiaECola = payment.getPointOfInteraction().getTransactionData().getQrCode();
            String base64 = payment.getPointOfInteraction().getTransactionData().getQrCodeBase64();

            log.info("✅ Pix gerado com sucesso no Mercado Pago! ID Transação: {}", paymentId);

            orderService.createOrder(
                    gift,
                    payerInfo.getName(),
                    payerInfo.getEmail(),
                    payerInfo.getCpf(),
                    payerInfo.getMessage(),
                    gift.getPrice(),
                    "PIX",
                    String.valueOf(paymentId),
                    "PENDENTE"
            );

            PixResponseDTO response = new PixResponseDTO(paymentId, copiaECola, base64, status);
            return ResponseEntity.ok(response);

        } catch (com.mercadopago.exceptions.MPApiException apiException) {
            String detalhesErro = apiException.getApiResponse().getContent();
            log.error("❌ O Mercado Pago recusou o pagamento. Motivo exato: {}", detalhesErro);
            return ResponseEntity.status(400).body(Map.of(
                    "message", "O Mercado Pago recusou a transação. Verifique o terminal para detalhes."
            ));
        } catch (Exception e) {
            log.error("❌ Erro interno no servidor: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Erro ao comunicar com o servidor de pagamentos."
            ));
        }
    }

    @PostMapping("/credit-card/{giftId}")
    public ResponseEntity<?> criarPagamentoCartao(@PathVariable Long giftId, @RequestBody CardPaymentDTO cardInfo) {
        try {
            log.info("💳 Processando Cartão para o presente ID {}. Pagador: {}", giftId, cardInfo.getName());

            Gift gift = giftService.findById(giftId);
            if (gift == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Presente não encontrado no catálogo."));
            }

            Payment payment = mercadoPagoService.gerarPagamentoCartao(gift.getName(), gift.getPrice(), cardInfo);

            log.info("✅ Resposta do Cartão recebida! Status: {}", payment.getStatus());

            String statusFinal = "PENDENTE";
            if ("approved".equalsIgnoreCase(payment.getStatus())) {
                statusFinal = "APROVADO";
            } else if ("rejected".equalsIgnoreCase(payment.getStatus())) {
                statusFinal = "RECUSADO";
            }

            orderService.createOrder(
                    gift,
                    cardInfo.getName(),
                    cardInfo.getEmail(),
                    cardInfo.getCpf(),
                    cardInfo.getMessage(),
                    gift.getPrice(),
                    "CREDIT_CARD",
                    String.valueOf(payment.getId()),
                    statusFinal
            );

            return ResponseEntity.ok(Map.of(
                    "status", payment.getStatus(),
                    "statusDetail", payment.getStatusDetail(),
                    "paymentId", payment.getId()
            ));

        } catch (com.mercadopago.exceptions.MPApiException apiException) {
            String detalhesErro = apiException.getApiResponse().getContent();
            log.error("❌ O Mercado Pago recusou o cartão. Motivo: {}", detalhesErro);
            return ResponseEntity.status(400).body(Map.of(
                    "message", "Pagamento recusado. Verifique os dados do cartão."
            ));
        } catch (Exception e) {
            log.error("❌ Erro interno no servidor (Cartão): ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Erro interno ao processar o cartão."
            ));
        }
    }

    /**
     * Rota de WEBHOOK (Notificações Assíncronas do Mercado Pago)
     * Exemplo de chamada do MP: POST /api/payments/webhook?data.id=12345678&type=payment
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> receberNotificacaoWebhook(
            @RequestParam(name = "data.id", required = false) String paymentId,
            @RequestParam(name = "type", required = false) String type,
            @RequestBody(required = false) Map<String, Object> payload) {

        log.info("🔔 Webhook acionado pelo Mercado Pago! Tipo: {} | ID: {}", type, paymentId);

        try {
            if ("payment".equalsIgnoreCase(type) && paymentId != null) {

                orderService.updateOrderStatusByMpId(paymentId, "APROVADO");
                log.info("✅ Pedido transação MP '{}' atualizado para APROVADO via Webhook!", paymentId);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.warn("⚠️ Não foi possível processar o webhook para o ID '{}': {}", paymentId, e.getMessage());
            return ResponseEntity.ok().build();
        }
    }
}