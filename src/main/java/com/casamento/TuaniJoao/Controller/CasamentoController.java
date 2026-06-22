package com.casamento.TuaniJoao.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class CasamentoController {

    @GetMapping
    public String adminPage() {
        return "admin";
    }
}
