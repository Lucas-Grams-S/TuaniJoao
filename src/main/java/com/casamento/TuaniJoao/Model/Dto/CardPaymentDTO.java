package com.casamento.TuaniJoao.Model.Dto;

import lombok.Data;

@Data
public class CardPaymentDTO extends  PaymentDTO {

    private String token;
    private String paymentMethodId;
    private Integer installments;
}