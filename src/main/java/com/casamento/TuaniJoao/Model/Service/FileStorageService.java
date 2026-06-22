package com.casamento.TuaniJoao.Model.Service;

import com.casamento.TuaniJoao.Exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    // Define a pasta raiz onde os arquivos serão salvos (na raiz do projeto por enquanto)
    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        log.info("Inicializando FileStorageService e criando pasta de uploads se necessário...");
        try {
            // Cria a pasta "uploads" automaticamente quando o sistema iniciar, se ela não existir
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.error("Erro ao criar a pasta de uploads: {}", e.getMessage());
            throw new RuntimeException("Não foi possível inicializar a pasta de uploads!");
        }
    }

    public String store(MultipartFile file) {
        log.info("Armazenando arquivo: {}", file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                log.error("Falha ao armazenar arquivo vazio: {}", file.getOriginalFilename());
                throw new BusinessRuleException("O arquivo enviado está vazio. Selecione uma imagem válida.");
            }

            // Gera um nome único para o arquivo. Ex: 123e4567-e89b-12d3_liquidificador.jpg
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Resolve o caminho final onde o arquivo será salvo
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

            log.info("Destino do arquivo: {}", destinationFile);

            // Copia o arquivo da requisição web para a pasta física
            Files.copy(file.getInputStream(), destinationFile);

            // Retorna o caminho virtual que será salvo no banco de dados
            return "/uploads/" + filename;

        } catch (IOException e) {
            log.error("Erro ao armazenar o arquivo {}: {}", file.getOriginalFilename(), e.getMessage());
            // Mantemos a mensagem amigável para o usuário, mas evitamos vazar detalhes técnicos do disco
            throw new BusinessRuleException("Falha técnica ao salvar a imagem. Tente novamente.");
        }
    }
}