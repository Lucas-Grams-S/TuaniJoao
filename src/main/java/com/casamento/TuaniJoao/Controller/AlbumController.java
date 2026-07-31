package com.casamento.TuaniJoao.Controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
        return "album";
    }

    /**
     * 🚀 Recebe a foto enviada pelo celular do convidado via AJAX/Fetch
     */
    @PostMapping("/api/album/upload")
    @ResponseBody
    public ResponseEntity<?> receberFotos(
            @RequestParam("fotos") List<MultipartFile> fotos,
            @RequestParam(value = "nome", required = false, defaultValue = "Convidado") String nome) {

        if (fotos == null || fotos.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nenhuma foto selecionada."));
        }

        if (fotos.size() > 10) {
            return ResponseEntity.badRequest().body(Map.of("message", "Por favor, envie no máximo 10 fotos por vez."));
        }

        try {
            Path pastaAbsoluta = pastaUploads.toAbsolutePath().normalize();
            if (!Files.exists(pastaAbsoluta)) {
                Files.createDirectories(pastaAbsoluta);
            }

            int salvasComSucesso = 0;

            for (MultipartFile foto : fotos) {
                if (foto.isEmpty()) continue;

                String extensao = obterExtensao(foto.getOriginalFilename());
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String nomeArquivo = "foto_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extensao;

                Path caminhoFinal = pastaAbsoluta.resolve(nomeArquivo);

                try (java.io.InputStream inputStream = foto.getInputStream()) {
                    Files.copy(inputStream, caminhoFinal, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    salvasComSucesso++;
                }
            }

            log.info("📸 {} foto(s) enviada(s) pelo convidado '{}'!", salvasComSucesso, nome);

            String mensagemRetorno = salvasComSucesso == 1
                    ? "1 foto enviada com sucesso para os noivos! ❤️"
                    : salvasComSucesso + " fotos enviadas com sucesso para os noivos! ❤️";

            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "message", mensagemRetorno
            ));

        } catch (Exception e) {
            log.error("❌ Erro ao salvar fotos do álbum: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Erro interno ao processar e salvar as fotos: " + e.getMessage()
            ));
        }
    }

    /**
     * 📦 GERA E FAZ DOWNLOAD DO ÁLBUM COMPLETO EM FORMATO .ZIP
     * Acessível em: /painel-noivos/album/download-zip
     */
    @GetMapping("/painel-noivos/album/download-zip")
    public void baixarTodasFotosZip(HttpServletResponse response) {
        try {
            Path pastaAbsoluta = pastaUploads.toAbsolutePath().normalize();

            if (!Files.exists(pastaAbsoluta) || Files.list(pastaAbsoluta).findAny().isEmpty()) {
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Ainda não há fotos enviadas para baixar no álbum!");
                return;
            }

            String nomeArquivoZip = "album-casamento-tuani-joao.zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivoZip + "\"");

            try (OutputStream out = response.getOutputStream();
                 ZipOutputStream zipOut = new ZipOutputStream(out)) {

                Files.list(pastaAbsoluta).forEach(caminhoArquivo -> {
                    if (Files.isRegularFile(caminhoArquivo)) {
                        try {
                            ZipEntry entry = new ZipEntry(caminhoArquivo.getFileName().toString());
                            zipOut.putNextEntry(entry);
                            Files.copy(caminhoArquivo, zipOut);
                            zipOut.closeEntry();
                        } catch (IOException e) {
                            log.error("Erro ao adicionar arquivo {} no ZIP: ", caminhoArquivo.getFileName(), e);
                        }
                    }
                });

                zipOut.finish();
            }

            log.info("📦 Download do ZIP do álbum concluído com sucesso!");

        } catch (Exception e) {
            log.error("❌ Erro ao gerar o arquivo ZIP do álbum: ", e);
        }
    }

    private String obterExtensao(String nomeOriginal) {
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            return nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        return ".jpg";
    }
}