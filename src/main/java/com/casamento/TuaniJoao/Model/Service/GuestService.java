package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Model.Entity.Guest;
import com.casamento.TuaniJoao.Exception.BusinessRuleException;
import com.casamento.TuaniJoao.Exception.ResourceNotFoundException;
import com.casamento.TuaniJoao.Model.Repository.GuestRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    // Utilitário para remover acentos
    private String removerAcentos(String str) {
        if (str == null) return "";
        String normalizado = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizado).replaceAll("").toLowerCase();
    }

    public List<Guest> findAll() {
        log.info("Buscando todos os convidados");
        return guestRepository.findAll();
    }

    // Busca normalizada ignorando acentos e maiúsculas/minúsculas
    public List<Guest> findByName(String name) {
        log.info("Buscando convidados ignorando acentos. Termo: {}", name);
        String termoBuscaLimpo = removerAcentos(name);

        return guestRepository.findAll().stream()
                .filter(guest -> removerAcentos(guest.getName()).contains(termoBuscaLimpo))
                .collect(Collectors.toList());
    }

    public Guest save(Guest guest) {
        return guestRepository.save(guest);
    }

    // Novo método de exclusão
    public void deleteGuest(Long id) {
        log.info("Excluindo convidado ID: {}", id);
        if (!guestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Convidado não encontrado no sistema.");
        }
        guestRepository.deleteById(id);
    }

    public void confirmAttendanceBatch(List<Long> ids) {
        List<Guest> selectedGuests = guestRepository.findAllById(ids);
        if (selectedGuests.size() != ids.size()) {
            throw new ResourceNotFoundException("Um ou mais convidados não foram encontrados.");
        }
        for (Guest guest : selectedGuests) {
            guest.setIsConfirmed(true);
        }
        guestRepository.saveAll(selectedGuests);
    }

    public void importGuestsFromCsv(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            List<Guest> guestsToSave = new ArrayList<>();
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] data = line.split(";|,");
                if (data.length > 0) {
                    String name = data[0].trim();
                    if (!name.isEmpty()) {
                        Guest guest = new Guest();
                        guest.setName(name);
                        guest.setIsConfirmed(false);
                        guestsToSave.add(guest);
                    }
                }
            }
            if (!guestsToSave.isEmpty()) {
                guestRepository.saveAll(guestsToSave);
            }
        } catch (Exception e) {
            throw new BusinessRuleException("Erro ao processar a planilha.");
        }
    }
}