package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Model.Dto.CardPaymentDTO;
import com.casamento.TuaniJoao.Model.Dto.GuestPaymentDTO;
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

    public Payment gerarPagamentoPix(String nomePresente, BigDecimal valor, GuestPaymentDTO payerInfo) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();

        // Limpa o CPF tirando pontos e traços para o Mercado Pago não reclamar
        String cleanCpf = payerInfo.getCpf().replaceAll("[^0-9]", "");

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valor)
                .description("Presente: " + nomePresente)
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .email(payerInfo.getEmail()) // E-mail digitado na tela
                        .firstName(payerInfo.getName()) // Nome digitado
                        .identification(IdentificationRequest.builder()
                                .type("CPF")
                                .number(cleanCpf) // CPF limpo
                                .build())
                        .build())
                .build();

        return client.create(request);
    }

    // Método para Cartão de Crédito
    public Payment gerarPagamentoCartao(String nomePresente, BigDecimal valor, CardPaymentDTO cardInfo) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();

        String cleanCpf = cardInfo.getCpf().replaceAll("[^0-9]", "");

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valor)
                .description("Presente de Casamento: " + nomePresente)
                .paymentMethodId(cardInfo.getPaymentMethodId()) // Bandeira (Visa, Master)
                .token(cardInfo.getToken())                     // Token de segurança
                .installments(cardInfo.getInstallments())       // Parcelas
                .payer(PaymentPayerRequest.builder()
                        .email(cardInfo.getEmail())
                        .firstName(cardInfo.getName())
                        .identification(IdentificationRequest.builder()
                                .type("CPF")
                                .number(cleanCpf)
                                .build())
                        .build())
                .build();

        return client.create(request);
    }
}