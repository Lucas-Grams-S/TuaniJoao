package com.casamento.TuaniJoao.Model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Data
@Entity()
@Table(name = "guests")
@AllArgsConstructor
@NoArgsConstructor
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean isConfirmed = false;

    @Column(name = "confirmation_date")
    private LocalDateTime confirmationDate;

    public void setIsConfirmed(Boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
        //todo: validar regra se os convidados podem desconfirmar
        if (isConfirmed != null && isConfirmed) {
            this.confirmationDate = LocalDateTime.now();
        } else {
            this.confirmationDate = null;
        }
    }
}
