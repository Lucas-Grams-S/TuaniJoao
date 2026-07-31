package com.casamento.TuaniJoao.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
public class AlbumController {

    // Pasta onde as fotos dos convidados serão salvas no disco/Docker
    private final Path pastaUploads = Paths.get("uploads/album-convidados");

    public AlbumController() {
        try {
            Files.createDirectories(pastaUploads);
        } catch (IOException e) {
            log.error("Erro ao criar diretório do álbum de convidados: ", e);
        }
    }

    /**
     * 📸 Abre a página do mural de fotos para os convidados
     */
    @GetMapping("/convidados/album")
    public String telaAlbumConvidados() {
        return "album"; // Aponta para templates/album.html
    }

    /**
     * 🚀 Recebe a foto enviada pelo celular do convidado via AJAX/Fetch
     */
    @PostMapping("/api/album/upload")
    @ResponseBody
    public ResponseEntity<?> receberFoto(
            @RequestParam("foto") MultipartFile foto,
            @RequestParam(value = "nome", required = false, defaultValue = "Convidado") String nome) {

        if (foto.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nenhuma foto selecionada."));
        }

        try {
            // Gera um nome único para o arquivo não sobrescrever outros
            String extensao = obterExtensao(foto.getOriginalFilename());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivo = "foto_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extensao;

            Path caminhoFinal = pastaUploads.resolve(nomeArquivo);
            foto.transferTo(caminhoFinal.toFile());

            log.info("📸 Nova foto enviada pelo convidado '{}'! Salva em: {}", nome, nomeArquivo);

            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "message", "Foto enviada com sucesso para os noivos! ❤️"
            ));

        } catch (Exception e) {
            log.error("❌ Erro ao salvar foto do álbum: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Erro interno ao processar e salvar a foto."
            ));
        }
    }

    private String obterExtensao(String nomeOriginal) {
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            return nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        return ".jpg"; // Padrão caso não identifique
    }
}