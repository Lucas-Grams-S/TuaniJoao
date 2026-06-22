package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Guest;
import com.casamento.TuaniJoao.Model.Service.GuestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GuestController.class)
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc; // Ferramenta para simular as requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // Ferramenta para transformar listas/objetos em JSON

    @MockitoBean
    private GuestService guestService; // Finge o serviço, pois o foco aqui é testar apenas as rotas

    @Test
    @DisplayName("Deve buscar convidados por nome e retornar 200 OK")
    void shouldSearchGuestsByName() throws Exception {
        // Arrange
        Guest guest = new Guest();
        guest.setId(1L);
        guest.setName("João");
        when(guestService.findByName("João")).thenReturn(List.of(guest));

        // Act & Assert (Dispara o GET e verifica a resposta)
        mockMvc.perform(get("/api/guests/search")
                        .param("name", "João")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("João"));
    }

    @Test
    @DisplayName("Deve listar todos os convidados e retornar 200 OK")
    void shouldGetAllGuests() throws Exception {
        // Arrange
        Guest guest1 = new Guest();
        guest1.setName("Maria");
        Guest guest2 = new Guest();
        guest2.setName("José");

        when(guestService.findAll()).thenReturn(Arrays.asList(guest1, guest2));

        // Act & Assert
        mockMvc.perform(get("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Maria"))
                .andExpect(jsonPath("$[1].name").value("José"));
    }

    @Test
    @DisplayName("Deve confirmar presença em lote recebendo um JSON e retornar 200 OK")
    void shouldConfirmAttendanceBatch() throws Exception {
        // Arrange
        List<Long> idsToConfirm = Arrays.asList(1L, 2L, 3L);
        String jsonPayload = objectMapper.writeValueAsString(idsToConfirm);

        // Como o método é 'void' no service, usamos o doNothing()
        doNothing().when(guestService).confirmAttendanceBatch(idsToConfirm);

        // Act & Assert (Dispara o POST enviando o JSON no corpo)
        mockMvc.perform(post("/api/guests/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve fazer upload do arquivo CSV e retornar 200 OK")
    void shouldUploadGuestsCsv() throws Exception {
        // Arrange
        // Cria um arquivo falso para enviar na requisição
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "lista.csv",
                "text/csv",
                "Nome\nJoao\nMaria".getBytes()
        );

        doNothing().when(guestService).importGuestsFromCsv(any());

        // Act & Assert (Dispara um POST multipart)
        mockMvc.perform(multipart("/api/guests/upload-csv")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Lista de convidados importada com sucesso!"));
    }
}