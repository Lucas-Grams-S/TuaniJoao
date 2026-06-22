package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Exception.BusinessRuleException;
import com.casamento.TuaniJoao.Exception.ResourceNotFoundException;
import com.casamento.TuaniJoao.Model.Entity.Guest;
import com.casamento.TuaniJoao.Model.Repository.GuestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private GuestService guestService;

    @Test
    @DisplayName("Deve confirmar a presença em lote com sucesso")
    void shouldConfirmAttendanceBatchSuccessfully() {
        // Arrange (Preparação)
        List<Long> ids = Arrays.asList(1L, 2L);

        Guest guest1 = new Guest();
        guest1.setId(1L);
        guest1.setIsConfirmed(false);

        Guest guest2 = new Guest();
        guest2.setId(2L);
        guest2.setIsConfirmed(false);

        // Quando o serviço pedir ao repositório pelos IDs, devolvemos a nossa lista falsa
        when(guestRepository.findAllById(ids)).thenReturn(Arrays.asList(guest1, guest2));

        // Act (Ação)
        guestService.confirmAttendanceBatch(ids);

        // Assert (Verificação)
        assertTrue(guest1.getIsConfirmed());
        assertTrue(guest2.getIsConfirmed());

        // Verifica se o método saveAll foi chamado exatamente uma vez com a lista correta
        verify(guestRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar confirmar IDs inexistentes")
    void shouldThrowExceptionWhenConfirmingInvalidIds() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 999L);

        Guest guest1 = new Guest();
        guest1.setId(1L);
        guest1.setIsConfirmed(false);

        // O repositório vai encontrar apenas 1 convidado, mas pedimos 2 IDs
        when(guestRepository.findAllById(ids)).thenReturn(List.of(guest1));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            guestService.confirmAttendanceBatch(ids);
        });

        assertEquals("Um ou mais convidados selecionados não foram encontrados no banco de dados.", exception.getMessage());

        // Verifica que o saveAll NUNCA foi chamado, protegendo o banco de dados
        verify(guestRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve buscar convidados por nome ignorando maiúsculas e minúsculas")
    void shouldFindGuestsByName() {
        // Arrange
        String searchName = "joão";
        Guest guest = new Guest();
        guest.setName("João Silva");

        when(guestRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(List.of(guest));

        // Act
        List<Guest> result = guestService.findByName(searchName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("João Silva", result.get(0).getName());
        verify(guestRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    @DisplayName("Deve retornar todos os convidados")
    void shouldFindAllGuests() {
        // Arrange
        when(guestRepository.findAll()).thenReturn(Arrays.asList(new Guest(), new Guest()));

        // Act
        List<Guest> result = guestService.findAll();

        // Assert
        assertEquals(2, result.size());
        verify(guestRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve salvar um convidado com sucesso")
    void shouldSaveGuest() {
        // Arrange
        Guest guest = new Guest();
        guest.setName("Tia Maria");
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);

        // Act
        Guest savedGuest = guestService.save(guest);

        // Assert
        assertNotNull(savedGuest);
        assertEquals("Tia Maria", savedGuest.getName());
        verify(guestRepository, times(1)).save(any(Guest.class));
    }

    // --- TESTES DO IMPORTADOR DE CSV ---

    @Test
    @DisplayName("Deve importar convidados de um CSV válido ignorando linhas vazias e o cabeçalho")
    void shouldImportGuestsFromValidCsv() throws Exception {
        // Arrange
        // Criamos o conteúdo de um CSV simulado (Cabeçalho + 2 nomes válidos + 1 linha vazia)
        String csvContent = "Nome\nJoão Silva\nMaria Souza\n\n   \n";

        // Mockamos o MultipartFile que vem da web
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvContent.getBytes()));
        when(mockFile.getOriginalFilename()).thenReturn("lista.csv");

        // Act
        guestService.importGuestsFromCsv(mockFile);

        // Assert
        // Verifica se o método saveAll foi chamado. Precisamos capturar a lista que foi passada pra ele.
        verify(guestRepository, times(1)).saveAll(argThat(list -> {
            List<Guest> guests = (List<Guest>) list;
            return guests.size() == 2 &&
                    guests.get(0).getName().equals("João Silva") &&
                    guests.get(1).getName().equals("Maria Souza");
        }));
    }

    @Test
    @DisplayName("Deve lançar exceção se ocorrer um erro de IO ao ler o CSV")
    void shouldThrowExceptionWhenCsvReadFails() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("arquivo_corrompido.csv");
        // Simulamos o arquivo estourando um erro de leitura físico
        when(mockFile.getInputStream()).thenThrow(new java.io.IOException("Disco com falha"));

        // Act & Assert
        // Verifica se a nossa conversão de erro (para BusinessRuleException ou RuntimeException) funciona
        Exception exception = assertThrows(BusinessRuleException.class, () -> {
            guestService.importGuestsFromCsv(mockFile);
        });

        assertTrue(exception.getMessage().contains("Erro ao processar a planilha"));

        // Garante que não tentou salvar nada no banco
        verify(guestRepository, never()).saveAll(anyList());
    }
}