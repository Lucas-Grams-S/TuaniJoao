package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Repository.Guest;
import com.casamento.TuaniJoao.Model.Service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests") // Define o caminho base para todas as rotas deste controller
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    /**
     * Rota para buscar convidados pelo nome.
     * Exemplo de uso: GET /api/guests/search?name=João
     */
    @GetMapping("/search")
    public ResponseEntity<List<Guest>> searchByName(@RequestParam String name) {
        List<Guest> guests = guestService.findByName(name);

        // Se a lista estiver vazia, o Spring retorna um array vazio [] com status 200 (OK)
        return ResponseEntity.ok(guests);
    }

    /**
     * Rota para confirmar a presença em lote.
     * Exemplo de uso: POST /api/guests/confirm
     * Corpo da requisição (JSON): [1, 2, 3]
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmAttendanceBatch(@RequestBody List<Long> ids) {
        guestService.confirmAttendanceBatch(ids);

        // Retorna status 200 (OK) sem corpo, indicando que a operação foi um sucesso
        return ResponseEntity.ok().build();
    }

    /**
     * Rota opcional para listar todos os convidados (útil para o painel dos noivos).
     * Exemplo de uso: GET /api/guests
     */
    @GetMapping
    public ResponseEntity<List<Guest>> getAllGuests() {
        return ResponseEntity.ok(guestService.findAll());
    }
}
