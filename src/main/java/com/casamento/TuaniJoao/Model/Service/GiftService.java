package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Repository.GiftRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GiftService {

    private final GiftRepository repository;

    public GiftService(GiftRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna a lista de presentes para exibir na tela do usuário.
     */
    public List<Gift> findAllGifts() {
        return repository.findAll();
    }

    /**
     * Método para salvar um novo presente (útil para o painel de admin dos noivos).
     */
    public Gift saveGift(Gift gift) {
        return repository.save(gift);
    }
}
