package com.casamento.TuaniJoao.Model.Dto;

import lombok.Data;

@Data
public abstract  class PaymentDTO {
    private String name;
    private String email;
    private String cpf;
    private String message;
}
