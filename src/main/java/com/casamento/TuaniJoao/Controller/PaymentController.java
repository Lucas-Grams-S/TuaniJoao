package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Dto.CardPaymentDTO;
import com.casamento.TuaniJoao.Model.Dto.GuestPaymentDTO;
import com.casamento.TuaniJoao.Model.Dto.PixResponseDTO;
import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import com.casamento.TuaniJoao.Model.Service.MercadoPagoService;
import com.mercadopago.resources.payment.Payment;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final GiftService giftService;
    private final MercadoPagoService mercadoPagoService;

    @PostMapping("/pix/{giftId}")
    public ResponseEntity<?> criarPagamentoPix(@PathVariable Long giftId, @RequestBody GuestPaymentDTO payerInfo) {
        try {
            log.info("🎯 Requisição recebida para Pix ID {}. Pagador: {}", giftId, payerInfo.getName());
            // 1. Busca o presente no banco de dados
            Gift gift = giftService.findById(giftId);
            if (gift == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Presente não encontrado no catálogo."));
            }

            // 2. Chama o serviço do Mercado Pago passando o nome e o preço real do presente
            Payment payment = mercadoPagoService.gerarPagamentoPix(gift.getName(), gift.getPrice(), payerInfo);

            // 3. Extrai os dados específicos do Pix de dentro do calhamaço de dados do Mercado Pago
            Long paymentId = payment.getId();
            String status = payment.getStatus();
            String copiaECola = payment.getPointOfInteraction().getTransactionData().getQrCode();
            String base64 = payment.getPointOfInteraction().getTransactionData().getQrCodeBase64();

            log.info("✅ Pix gerado com sucesso no Mercado Pago! ID Transação: {}", paymentId);

            // 4. Monta o DTO limpo e envia para o Frontend
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

            // Devolve o status ("approved", "rejected", "in_process") e os detalhes
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
}
