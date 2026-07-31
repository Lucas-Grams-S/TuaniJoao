package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Guest;
import com.casamento.TuaniJoao.Model.Service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    /**
     * Rota para buscar convidados pelo nome.
     * Exemplo de uso: GET /api/guests/search?name=João
     */
    @GetMapping("/search")
    public ResponseEntity<List<Guest>> searchByName(@RequestParam String name) {
        log.info("Buscando convidados com nome contendo: {}", name);
        List<Guest> guests = guestService.findByName(name);

        return ResponseEntity.ok(guests);
    }

    /**
     * Rota para confirmar a presença em lote.
     * Exemplo de uso: POST /api/guests/confirm
     * Corpo da requisição (JSON): [1, 2, 3]
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmAttendanceBatch(@RequestBody List<Long> ids) {
        log.info("Confirmando presença para os convidados com IDs: {}", ids);
        guestService.confirmAttendanceBatch(ids);

        return ResponseEntity.ok().build();
    }

    /**
     * Rota opcional para listar todos os convidados (útil para o painel dos noivos).
     * Exemplo de uso: GET /api/guests
     */
    @GetMapping
    public ResponseEntity<List<Guest>> getAllGuests() {
        log.info("Buscando todos os convidados");
        return ResponseEntity.ok(guestService.findAll());
    }

    /**
     * Rota para fazer upload da planilha CSV de convidados.
     * Exemplo de uso: POST /api/guests/upload-csv
     */
    @PostMapping("/upload-csv")
    public ResponseEntity<String> uploadGuestsCsv(@RequestParam("file") MultipartFile file) {
        log.info("Recebendo arquivo CSV para importação: {}", file.getOriginalFilename());
        guestService.importGuestsFromCsv(file);

        return ResponseEntity.ok("Lista de convidados importada com sucesso!");
    }

    /**
     * Rota para excluir um convidado.
     * Exemplo de uso: DELETE /api/guests/5
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        log.info("Requisição recebida para excluir convidado ID: {}", id);
        guestService.deleteGuest(id);

        return ResponseEntity.noContent().build();
    }
}
