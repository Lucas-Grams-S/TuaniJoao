package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Service.GiftService;
import com.casamento.TuaniJoao.Model.Service.GuestService;
import com.casamento.TuaniJoao.Model.Service.OrderService;
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

    private final OrderService orderService;

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

    /**
     * 🛒 Rota para visualizar o Histórico de Presentes Comprados / Pedidos
     */
    @GetMapping("/orders")
    public String verHistoricoPedidos(Model model) {
        // Busca todos os pedidos ordenados pelos mais recentes primeiro
        model.addAttribute("orders", orderService.findAllOrders());
        return "painel-noivos-list-orders"; // Aponta para o novo template que criaremos no Passo 2
    }

}
