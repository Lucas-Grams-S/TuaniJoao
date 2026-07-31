package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import java.io.File;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/convidados") // Path centralizado para os convidados
public class GuestPublicController {

    @Autowired
    private GiftService giftService;

    // 🎁 Rota 1: /convidados/presentes
    @GetMapping("/presentes")
    public String telaPublicaPresentes(Model model) {
        List<Gift> gifts = giftService.findAllGifts();
        model.addAttribute("gifts", gifts);
        model.addAttribute("fotosCarrossel", listarFotosCarrossel());
        return "presentes"; // Aponta para o templates/presentes.html
    }

    // ✅ Rota 2: /convidados/confirmar-presenca (Faremos a tela a seguir)
    @GetMapping("/confirmar-presenca")
    public String telaPublicaRsvp(Model model) {
        model.addAttribute("fotosCarrossel", listarFotosCarrossel());
        return "rsvp";
    }

    // ℹ️ Rota 3: /convidados/informacoes (Faremos a tela a seguir)
    @GetMapping("/informacoes")
    public String telaPublicaInformacoes(Model model) {
        model.addAttribute("fotosCarrossel", listarFotosCarrossel());
        return "informacoes";
    }

    private List<String> listarFotosCarrossel() {
        List<String> fotos = new ArrayList<>();
        try {
            // Aponta para a pasta static/images/carrossel
            File pasta = new ClassPathResource("static/images").getFile();
            if (pasta.exists() && pasta.isDirectory()) {
                File[] arquivos = pasta.listFiles();
                if (arquivos != null) {
                    for (File arquivo : arquivos) {
                        // Filtra apenas arquivos de imagem
                        String nome = arquivo.getName().toLowerCase();
                        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg") ||
                                nome.endsWith(".png") || nome.endsWith(".webp")) {
                            fotos.add("/images/" + arquivo.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Se a pasta não for encontrada ou estiver vazia, evita quebrar a tela
            System.out.println("Aviso: Não foi possível ler a pasta do carrossel: " + e.getMessage());
        }
        return fotos;
    }
}