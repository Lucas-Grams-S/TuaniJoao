package com.casamento.TuaniJoao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Entity()
@Table(name = "convidao")
@AllArgsConstructor
public class Convidado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Boolean confirmado = false;

    @Column(name = "data_hora_confirmacao")
    private LocalDateTime dataHoraConfirmacao;

    public void setConfirmado(Boolean confirmado) {
        this.confirmado = confirmado;
        //todo: validar regra se os convidados podem desconfirmar
        if (confirmado != null && confirmado) {
            this.dataHoraConfirmacao = LocalDateTime.now();
        } else {
            this.dataHoraConfirmacao = null;
        }
    }
}
