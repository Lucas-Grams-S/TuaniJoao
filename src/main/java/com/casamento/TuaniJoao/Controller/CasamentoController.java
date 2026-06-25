package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Service.GiftService;
import com.casamento.TuaniJoao.Model.Service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/painel-noivos")
@RequiredArgsConstructor
public class CasamentoController {

    private final GiftService giftService;

    private final GuestService guestService;

    @GetMapping
    public String adminPage() {
        return "painel-noivos";
    }

    @GetMapping("/gifts")
    public String listGifts(Model model) {
        model.addAttribute("gifts", giftService.findAllGifts());
        return "painel-noivos-list-gifts";
    }

    @GetMapping("/guests")
    public String listGuests(Model model) {
        model.addAttribute("guests", guestService.findAll());
        return "painel-noivos-list-guests";
    }

}
