package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Exception.BusinessRuleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileStorageServiceTest {

    // Como esta classe não tem dependências injetadas (não tem repositórios),
    // nós instanciamos ela diretamente, sem precisar do @InjectMocks
    private final FileStorageService fileStorageService = new FileStorageService();

    // Variável para guardar o caminho do arquivo criado no teste e apagá-lo depois
    private Path createdFilePath;

    @AfterEach
    void tearDown() throws IOException {
        // Limpeza: Se o teste criou um arquivo físico, nós o deletamos para não sujar o projeto
        if (createdFilePath != null) {
            Files.deleteIfExists(createdFilePath);
        }
    }

    @Test
    @DisplayName("Deve armazenar um arquivo válido com sucesso e retornar o caminho gerado")
    void shouldStoreValidFileSuccessfully() {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "foto-teste.jpg",
                "image/jpeg",
                "conteudo_falso_da_imagem".getBytes()
        );

        // Act
        String resultPath = fileStorageService.store(mockFile);

        // Assert
        assertNotNull(resultPath);
        assertTrue(resultPath.startsWith("/uploads/"));
        assertTrue(resultPath.endsWith("_foto-teste.jpg"));

        // Prepara para a limpeza (remove a primeira barra '/' para mapear pro disco local)
        createdFilePath = Paths.get(resultPath.substring(1));

        // Verifica fisicamente se o arquivo foi criado na pasta do seu computador
        assertTrue(Files.exists(createdFilePath));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao tentar armazenar arquivo vazio")
    void shouldThrowExceptionWhenFileIsEmpty() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "arquivo-vazio.txt",
                "text/plain",
                new byte[0] // Conteúdo com zero bytes
        );

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            fileStorageService.store(emptyFile);
        });

        assertEquals("O arquivo enviado está vazio. Selecione uma imagem válida.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao ocorrer falha de IO (Erro de leitura/escrita)")
    void shouldThrowExceptionWhenIoErrorOccurs() throws IOException {
        // Arrange
        // Aqui usamos o Mockito para forçar o arquivo a dar erro na hora de ser lido
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.isEmpty()).thenReturn(false);
        when(badFile.getOriginalFilename()).thenReturn("foto-bugada.jpg");
        when(badFile.getInputStream()).thenThrow(new IOException("Disco arranhado"));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            fileStorageService.store(badFile);
        });

        assertEquals("Falha técnica ao salvar a imagem. Tente novamente.", exception.getMessage());
    }
}
