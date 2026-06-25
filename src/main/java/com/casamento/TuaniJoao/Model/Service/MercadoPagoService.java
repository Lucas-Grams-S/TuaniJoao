package com.casamento.TuaniJoao.Model.Service;

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
        // O "Client" é o carteiro do Mercado Pago que envia a nossa carta
        PaymentClient client = new PaymentClient();

        // Aqui nós montamos a carta (A requisição de pagamento)
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valor) // Valor real do presente
                .description("Presente de Casamento: " + nomePresente)
                .paymentMethodId("pix") // Define que queremos Pix e não boleto/cartão
                .payer(PaymentPayerRequest.builder()
                        .email("convidado_teste@gmail.com") // Em testes, o MP exige um e-mail válido no formato
                        .firstName("Convidado")
                        .build())
                .build();

        // Envia para o Mercado Pago e recebe a resposta completa (com o QR Code)
        return client.create(request);
    }
}