package com.casamento.TuaniJoao.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PixResponseDTO {
    private Long paymentId;          // ID da transação no Mercado Pago (útil para consultas futuras)
    private String qrCodeCopiaECola; // O texto alfanumérico para o "Pix Copia e Cola"
    private String qrCodeBase64;     // A imagem do QR Code convertida em texto para o HTML desenhar
    private String status;           // Status atual (ex: PENDING)
}