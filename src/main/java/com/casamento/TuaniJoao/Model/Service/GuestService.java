package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Model.Repository.Guest;
import com.casamento.TuaniJoao.Model.Repository.GuestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    public List<Guest> findAll() {
        return guestRepository.findAll();
    }

    public List<Guest> findByName(String name) {
        return guestRepository.findByNameContainingIgnoreCase(name);
    }

    public Guest save(Guest guest) {
        return guestRepository.save(guest);
    }

    public void confirmAttendanceBatch(List<Long> ids) {
        // 1. Busca todos os convidados correspondentes aos IDs recebidos
        List<Guest> selectedGuests = guestRepository.findAllById(ids);

        // 2. Altera a flag de todos para true (a data é preenchida automaticamente pela entidade)
        for (Guest guest : selectedGuests) {
            guest.setIsConfirmed(true);
        }

        // 3. Salva a lista atualizada no banco de dados de uma vez só
        guestRepository.saveAll(selectedGuests);
    }
}
