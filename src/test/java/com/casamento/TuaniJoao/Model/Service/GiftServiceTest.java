package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Exception.ResourceNotFoundException;
import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Repository.GiftRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftServiceTest {

    @Mock
    private GiftRepository giftRepository;

    @InjectMocks
    private GiftService giftService;

    @Test
    @DisplayName("Deve retornar todos os presentes")
    void shouldFindAllGifts() {
        // Arrange
        Gift gift1 = new Gift();
        gift1.setName("Geladeira");
        Gift gift2 = new Gift();
        gift2.setName("Fogão");

        when(giftRepository.findAll()).thenReturn(Arrays.asList(gift1, gift2));

        // Act
        List<Gift> result = giftService.findAllGifts();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Geladeira", result.get(0).getName());
        verify(giftRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve salvar um único presente com sucesso")
    void shouldSaveGift() {
        // Arrange
        Gift giftToSave = new Gift();
        giftToSave.setName("Micro-ondas");
        giftToSave.setPrice(new BigDecimal("500.00"));

        when(giftRepository.save(any(Gift.class))).thenReturn(giftToSave);

        // Act
        Gift savedGift = giftService.saveGift(giftToSave);

        // Assert
        assertNotNull(savedGift);
        assertEquals("Micro-ondas", savedGift.getName());
        verify(giftRepository, times(1)).save(any(Gift.class));
    }

    @Test
    @DisplayName("Deve salvar um lote de presentes")
    void shouldSaveAllGiftsBatch() {
        // Arrange
        Gift gift1 = new Gift();
        Gift gift2 = new Gift();
        List<Gift> giftsList = Arrays.asList(gift1, gift2);

        when(giftRepository.saveAll(giftsList)).thenReturn(giftsList);

        // Act
        List<Gift> savedGifts = giftService.saveAllGifts(giftsList);

        // Assert
        assertEquals(2, savedGifts.size());
        verify(giftRepository, times(1)).saveAll(giftsList);
    }

    @Test
    @DisplayName("Deve atualizar a URL da foto de um presente existente")
    void shouldUpdatePhotoUrlSuccessfully() {
        // Arrange
        Long giftId = 1L;
        String newPhotoUrl = "/uploads/nova-foto.jpg";

        Gift existingGift = new Gift();
        existingGift.setId(giftId);
        existingGift.setName("Batedeira");
        existingGift.setPhotoUrl("/uploads/foto-antiga.jpg");

        // Simula que encontrou o presente no banco
        when(giftRepository.findById(giftId)).thenReturn(Optional.of(existingGift));
        // Simula o salvamento
        when(giftRepository.save(any(Gift.class))).thenReturn(existingGift);

        // Act
        Gift updatedGift = giftService.updatePhotoUrl(giftId, newPhotoUrl);

        // Assert
        assertEquals(newPhotoUrl, updatedGift.getPhotoUrl());
        verify(giftRepository, times(1)).findById(giftId);
        verify(giftRepository, times(1)).save(existingGift);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar foto de presente inexistente")
    void shouldThrowExceptionWhenUpdatingPhotoForNonExistentGift() {
        // Arrange
        Long invalidGiftId = 99L;
        String newPhotoUrl = "/uploads/foto.jpg";

        // Simula que NÃO encontrou no banco
        when(giftRepository.findById(invalidGiftId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            giftService.updatePhotoUrl(invalidGiftId, newPhotoUrl);
        });

        assertEquals("Presente não encontrado com o ID: " + invalidGiftId, exception.getMessage());

        // Garante que o método save() nunca foi chamado, protegendo o banco
        verify(giftRepository, never()).save(any(Gift.class));
    }
}