package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Model.Entity.Guest;
import com.casamento.TuaniJoao.Exception.BusinessRuleException;
import com.casamento.TuaniJoao.Exception.ResourceNotFoundException;
import com.casamento.TuaniJoao.Model.Repository.GuestRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    public List<Guest> findAll() {
        log.info("Buscando todos os convidados");
        return guestRepository.findAll();
    }

    public List<Guest> findByName(String name) {
        log.info("Buscando convidados com nome contendo: {}", name);
        return guestRepository.findByNameContainingIgnoreCase(name);
    }

    public Guest save(Guest guest) {
        log.info("Salvando convidado: {}", guest.getName());
        return guestRepository.save(guest);
    }

    public void confirmAttendanceBatch(List<Long> ids) {
        log.info("Confirmando presença para os convidados com IDs: {}", ids);

        List<Guest> selectedGuests = guestRepository.findAllById(ids);

        // Melhoria: Valida se todos os IDs enviados realmente existem no banco
        if (selectedGuests.size() != ids.size()) {
            log.warn("Tentativa de confirmar convidados com IDs inexistentes.");
            throw new ResourceNotFoundException("Um ou mais convidados selecionados não foram encontrados no banco de dados.");
        }

        for (Guest guest : selectedGuests) {
            guest.setIsConfirmed(true);
        }

        log.info("Salvando confirmação de presença para {} convidados", selectedGuests.size());
        guestRepository.saveAll(selectedGuests);
    }

    /**
     * Lê um arquivo CSV e salva os convidados em lote.
     * Espera-se que o CSV tenha uma coluna "Nome".
     */
    public void importGuestsFromCsv(MultipartFile file) {
        log.info("Importando convidados do arquivo CSV: {}", file.getOriginalFilename());
        // Usa o try-with-resources para garantir que o arquivo será fechado ao final
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            List<Guest> guestsToSave = new ArrayList<>();
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Pula a primeira linha se for o cabeçalho (ex: "Nome")
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // O Excel em português costuma exportar CSV separando por ponto e vírgula (;).
                // O padrão americano é vírgula (,). Vamos tratar as linhas simples.
                String[] data = line.split(";|,");

                if (data.length > 0) {
                    String name = data[0].trim();

                    if (!name.isEmpty()) {
                        Guest guest = new Guest();
                        guest.setName(name);
                        guest.setIsConfirmed(false); // Por padrão, ninguém chega confirmado
                        guestsToSave.add(guest);
                    }
                }
            }

            log.info("Total de convidados importados do CSV: {}", guestsToSave.size());
            // Salva todo o lote no banco de dados de uma vez só para ter alta performance
            if (!guestsToSave.isEmpty()) {
                log.info("Salvando {} convidados importados do CSV", guestsToSave.size());
                guestRepository.saveAll(guestsToSave);
            }

        } catch (Exception e) {
            log.error("Erro ao processar o arquivo CSV: {}", e.getMessage());
            throw new BusinessRuleException("Erro ao processar a planilha. Verifique se o formato é um CSV válido.");
        }
    }
}
