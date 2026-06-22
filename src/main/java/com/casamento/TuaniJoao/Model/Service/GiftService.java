package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Exception.ResourceNotFoundException;
import com.casamento.TuaniJoao.Model.Repository.GiftRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftService {

    private final GiftRepository repository;

    /**
     * Retorna a lista de presentes para exibir na tela do usuário.
     */
    public List<Gift> findAllGifts() {
        log.info("Buscando todos os presentes disponíveis");
        return repository.findAll();
    }

    /**
     * Método para salvar um novo presente (útil para o painel de admin dos noivos).
     */
    public Gift saveGift(Gift gift) {
        log.info("Salvando presente: {}", gift.getName());
        return repository.save(gift);
    }

    /**
     * Salva uma lista de presentes em lote (ideal para o painel admin).
     */
    public List<Gift> saveAllGifts(List<Gift> gifts) {
        log.info("Salvando lote de presentes: {} presentes", gifts.size());
        return repository.saveAll(gifts);
    }

    /**
     * Atualiza a URL da foto de um presente específico.
     */
    public Gift updatePhotoUrl(Long id, String photoUrl) {
        log.info("Atualizando URL da foto para o presente com ID: {}", id);
        Gift gift = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Presente não encontrado com ID: {}", id);
                    return new ResourceNotFoundException("Presente não encontrado com o ID: " + id);
                });

        gift.setPhotoUrl(photoUrl);
        log.info("URL da foto atualizada para o presente '{}': {}", gift.getName(), photoUrl);
        return repository.save(gift);
    }
}
