package com.casamento.TuaniJoao.Controller;

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
    public ResponseEntity<?> criarPagamentoPix(@PathVariable Long giftId) {
        try {
            log.info("🎯 Requisição recebida para gerar Pix para o presente ID: {}", giftId);

            // 1. Busca o presente no banco de dados
            Gift gift = giftService.findById(giftId);
            if (gift == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Presente não encontrado no catálogo."));
            }

            // 2. Chama o serviço do Mercado Pago passando o nome e o preço real do presente
            Payment payment = mercadoPagoService.gerarPagamentoPix(gift.getName(), gift.getPrice());

            // 3. Extrai os dados específicos do Pix de dentro do calhamaço de dados do Mercado Pago
            Long paymentId = payment.getId();
            String status = payment.getStatus();
            String copiaECola = payment.getPointOfInteraction().getTransactionData().getQrCode();
            String base64 = payment.getPointOfInteraction().getTransactionData().getQrCodeBase64();

            log.info("✅ Pix gerado com sucesso no Mercado Pago! ID Transação: {}", paymentId);

            // 4. Monta o DTO limpo e envia para o Frontend
            PixResponseDTO response = new PixResponseDTO(paymentId, copiaECola, base64, status);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erro crítico ao processar pagamento Pix: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Erro ao comunicar com o Mercado Pago: " + e.getMessage()
            ));
        }
    }
}
