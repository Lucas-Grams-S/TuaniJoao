package com.casamento.TuaniJoao.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PixResponseDTO {
    private Long paymentId;
    private String qrCodeCopiaECola;
    private String qrCodeBase64;
    private String status;
}