package mx.uaz.sqa.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/proyectos";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
