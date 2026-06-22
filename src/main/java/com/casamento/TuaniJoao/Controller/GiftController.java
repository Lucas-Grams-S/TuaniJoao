package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.FileStorageService;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;
    private final FileStorageService fileStorageService;

    /**
     * Rota para buscar todos os presentes.
     * Exemplo de uso: GET /api/gifts
     */
    @GetMapping
    public ResponseEntity<List<Gift>> getAllGifts() {
        log.info("Buscando todos os presentes");
        return ResponseEntity.ok(giftService.findAllGifts());
    }


    /**
     * Rota para fazer upload da foto ANTES de salvar o presente.
     * Retorna apenas a String com a URL gerada.
     */
    @PostMapping("/photo")
    public ResponseEntity<String> uploadPhotoOnly(@RequestParam("file") MultipartFile file) {
        log.info("Recebendo arquivo para upload: {}", file.getOriginalFilename());
        String photoUrl = fileStorageService.store(file);
        return ResponseEntity.ok(photoUrl);
    }

    /**
     * Rota para cadastrar um lote de presentes (Admin).
     * Exemplo de uso: POST /api/gifts/batch
     * Corpo: [{ "name": "Liquidificador", "price": 200, "photoUrl": "/uploads/123.jpg" }, {...}]
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Gift>> createGiftsBatch(@RequestBody List<Gift> gifts) {
        log.info("Recebendo lote de presentes para cadastro: {} presentes", gifts.size());
        return ResponseEntity.ok(giftService.saveAllGifts(gifts));
    }

    /**
     * Rota para cadastrar um novo presente (Admin).
     * Exemplo de uso: POST /api/gifts
     */
    @PostMapping
    public ResponseEntity<Gift> createGift(@RequestBody Gift gift) {
        log.info("Recebendo presente para cadastro: {}", gift.getName());
        return ResponseEntity.ok(giftService.saveGift(gift));
    }
}