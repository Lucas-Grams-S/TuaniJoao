package com.casamento.TuaniJoao.Model.Service;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MercadoPagoService {

    public Payment gerarPagamentoPix(String nomePresente, BigDecimal valor) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();

        // Truque para o Mercado Pago não achar que você está comprando de si mesmo
        String emailDinamico = "convidado_" + System.currentTimeMillis() + "@teste.com";

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valor)
                .description("Presente de Casamento: " + nomePresente)
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .email(emailDinamico)
                        .firstName("Convidado")
                        .lastName("Teste")
                        // REGRA DO PIX: Identificação (CPF) é obrigatória!
                        .identification(IdentificationRequest.builder()
                                .type("CPF")
                                .number("19119119100") // CPF genérico aceito no ambiente de testes
                                .build())
                        .build())
                .build();

        return client.create(request);
    }
}