package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gifts")
public class GiftController {

    private final GiftService giftService;

    public GiftController(GiftService giftService) {
        this.giftService = giftService;
    }

    /**
     * Rota para buscar todos os presentes.
     * Exemplo de uso: GET /api/gifts
     */
    @GetMapping
    public ResponseEntity<List<Gift>> getAllGifts() {
        return ResponseEntity.ok(giftService.findAllGifts());
    }

    /**
     * Rota para cadastrar um novo presente (Admin).
     * Exemplo de uso: POST /api/gifts
     */
    @PostMapping
    public ResponseEntity<Gift> createGift(@RequestBody Gift gift) {
        return ResponseEntity.ok(giftService.saveGift(gift));
    }
}