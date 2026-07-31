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

    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        log.info("Inicializando FileStorageService e criando pasta de uploads se necessário...");
        try {
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

            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

            log.info("Destino do arquivo: {}", destinationFile);

            Files.copy(file.getInputStream(), destinationFile);

            return "/uploads/" + filename;

        } catch (IOException e) {
            log.error("Erro ao armazenar o arquivo {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new BusinessRuleException("Falha técnica ao salvar a imagem. Tente novamente.");
        }
    }
}