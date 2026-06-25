package com.casamento.TuaniJoao.Config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MercadoPagoSetup {

    // Lê a chave que configuramos no application.yml / .env
    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        // Injeta a chave no motor do Mercado Pago assim que a aplicação liga
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("💳 SDK do Mercado Pago inicializado com sucesso!");
    }
}