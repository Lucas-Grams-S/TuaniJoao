package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Model.Dto.CardPaymentDTO;
import com.casamento.TuaniJoao.Model.Dto.GuestPaymentDTO;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MercadoPagoService {

    /**
     * Gera opções de requisição contendo uma chave de idempotência única.
     */
    private MPRequestOptions createIdempotencyOptions() {
        return MPRequestOptions.builder()
                .customHeaders(java.util.Map.of("X-Idempotency-Key", UUID.randomUUID().toString()))
                .build();
    }

    public Payment gerarPagamentoPix(String nomePresente, BigDecimal valor, GuestPaymentDTO payerInfo) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();
        String cleanCpf = payerInfo.getCpf().replaceAll("[^0-9]", "");

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valor)
                .description("Presente: " + nomePresente)
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .email(payerInfo.getEmail())
                        .firstName(payerInfo.getName())
                        .identification(IdentificationRequest.builder()
                                .type("CPF")
                                .number(cleanCpf)
                                .build())
                        .build())
                .build();

        // Passamos as opções com o X-Idempotency-Key
        return client.create(request, createIdempotencyOptions());
    }

    public Payment gerarPagamentoCartao(String nomePresente, BigDecimal valor, CardPaymentDTO cardInfo) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();
        String cleanCpf = cardInfo.getCpf().replaceAll("[^0-9]", "");

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valor)
                .description("Presente de Casamento: " + nomePresente)
                .paymentMethodId(cardInfo.getPaymentMethodId())
                .token(cardInfo.getToken())
                .installments(cardInfo.getInstallments())
                .payer(PaymentPayerRequest.builder()
                        .email(cardInfo.getEmail())
                        .firstName(cardInfo.getName())
                        .identification(IdentificationRequest.builder()
                                .type("CPF")
                                .number(cleanCpf)
                                .build())
                        .build())
                .build();

        // Passamos as opções com o X-Idempotency-Key
        return client.create(request, createIdempotencyOptions());
    }
}