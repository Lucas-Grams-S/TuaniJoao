package com.casamento.TuaniJoao.Model.Dto;

import lombok.Data;

@Data
public class CardPaymentDTO extends  PaymentDTO {

    private String token;             // O código criptografado do cartão
    private String paymentMethodId;   // A bandeira (ex: "visa", "master")
    private Integer installments;     // O número de parcelas (para testes, enviaremos 1)
}