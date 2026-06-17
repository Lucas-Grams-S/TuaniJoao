package com.casamento.TuaniJoao.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CasamentoController {

    @GetMapping("/")
    public String home() {
        return "bem vindo ao casamento dos caebcas";
    }

    @GetMapping("/info")
    public String info() {
        return "info";
    }

    @GetMapping("/confirmacao")
    public String confirmarPresenca() {
        return "confirmar presenca";
    }

}
