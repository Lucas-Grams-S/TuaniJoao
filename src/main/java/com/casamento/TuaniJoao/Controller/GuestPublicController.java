package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/convidados") // Path centralizado para os convidados
public class GuestPublicController {

    @Autowired
    private GiftService giftService;

    // 🎁 Rota 1: /convidados/presentes
    @GetMapping("/presentes")
    public String telaPublicaPresentes(Model model) {
        List<Gift> gifts = giftService.findAllGifts();
        model.addAttribute("gifts", gifts);
        return "presentes"; // Aponta para o templates/presentes.html
    }

    // ✅ Rota 2: /convidados/confirmar-presenca (Faremos a tela a seguir)
    @GetMapping("/confirmar-presenca")
    public String telaPublicaRsvp() {
        return "rsvp";
    }

    // ℹ️ Rota 3: /convidados/informacoes (Faremos a tela a seguir)
    @GetMapping("/informacoes")
    public String telaPublicaInformacoes() {
        return "informacoes";
    }
}