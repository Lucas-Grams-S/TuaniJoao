package com.casamento.TuaniJoao.Controller;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import com.casamento.TuaniJoao.Model.Service.GiftService;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/convidados")
public class GuestPublicController {

    @Autowired
    private GiftService giftService;

    @GetMapping("/presentes")
    public String telaPublicaPresentes(Model model) {
        List<Gift> gifts = giftService.findAllGifts();
        model.addAttribute("gifts", gifts);
        model.addAttribute("fotosCarrossel", listarFotosCarrossel());
        return "presentes"; // Aponta para o templates/presentes.html
    }

    @GetMapping("/confirmar-presenca")
    public String telaPublicaRsvp(Model model) {
        model.addAttribute("fotosCarrossel", listarFotosCarrossel());
        return "rsvp";
    }

    @GetMapping("/informacoes")
    public String telaPublicaInformacoes(Model model) {
        model.addAttribute("fotosCarrossel", listarFotosCarrossel());
        return "informacoes";
    }

    private List<String> listarFotosCarrossel() {
        List<String> fotos = new ArrayList<>();
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            Resource[] resources = resolver.getResources("classpath*:static/images/*.*");

            for (Resource resource : resources) {
                String nome = resource.getFilename();
                if (nome != null) {
                    String lower = nome.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                            lower.endsWith(".png") || lower.endsWith(".webp")) {
                        fotos.add("/images/" + nome);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Aviso: Não foi possível ler a pasta do carrossel: " + e.getMessage());
        }
        return fotos;
    }
}