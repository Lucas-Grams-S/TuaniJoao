package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.FileStorageService;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GiftController.class)
class GiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Usando a nova anotação do Spring Boot 3.4+ para os mocks
    @MockitoBean
    private GiftService giftService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("Deve buscar todos os presentes e retornar 200 OK")
    void shouldGetAllGifts() throws Exception {
        // Arrange
        Gift gift1 = new Gift();
        gift1.setId(1L);
        gift1.setName("Liquidificador");
        gift1.setPrice(new BigDecimal("250.00"));

        Gift gift2 = new Gift();
        gift2.setId(2L);
        gift2.setName("Jogo de Panelas");
        gift2.setPrice(new BigDecimal("500.00"));

        when(giftService.findAllGifts()).thenReturn(Arrays.asList(gift1, gift2));

        // Act & Assert
        mockMvc.perform(get("/api/gifts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Liquidificador"))
                .andExpect(jsonPath("$[1].name").value("Jogo de Panelas"));
    }

    @Test
    @DisplayName("Deve fazer upload da foto de um presente e retornar a URL com 200 OK")
    void shouldUploadPhotoOnly() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "foto.jpg",
                "image/jpeg",
                "imagem_falsa".getBytes()
        );

        String expectedUrl = "/uploads/uuid-foto.jpg";
        when(fileStorageService.store(any())).thenReturn(expectedUrl);

        // Act & Assert
        mockMvc.perform(multipart("/api/gifts/photo")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedUrl));
    }

    @Test
    @DisplayName("Deve cadastrar um único presente e retornar 200 OK")
    void shouldCreateSingleGift() throws Exception {
        // Arrange
        Gift newGift = new Gift();
        newGift.setName("Torradeira");
        newGift.setPrice(new BigDecimal("150.00"));

        Gift savedGift = new Gift();
        savedGift.setId(10L);
        savedGift.setName("Torradeira");
        savedGift.setPrice(new BigDecimal("150.00"));

        when(giftService.saveGift(any(Gift.class))).thenReturn(savedGift);

        String jsonPayload = objectMapper.writeValueAsString(newGift);

        // Act & Assert
        mockMvc.perform(post("/api/gifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Torradeira"));
    }

    @Test
    @DisplayName("Deve cadastrar um lote de presentes e retornar 200 OK")
    void shouldCreateGiftsBatch() throws Exception {
        // Arrange
        Gift gift1 = new Gift();
        gift1.setName("Mesa");

        Gift gift2 = new Gift();
        gift2.setName("Cadeira");

        List<Gift> giftsList = Arrays.asList(gift1, gift2);

        when(giftService.saveAllGifts(anyList())).thenReturn(giftsList);

        String jsonPayload = objectMapper.writeValueAsString(giftsList);

        // Act & Assert
        mockMvc.perform(post("/api/gifts/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Mesa"));
    }
}