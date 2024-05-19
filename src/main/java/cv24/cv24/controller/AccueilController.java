package cv24.cv24.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilController {
    @GetMapping("/accueil")
    public String getAccueil(Model model) {

        return "accueil";
    }

    @GetMapping("/help")
    public String getHelp(Model model) {

        return "help";
    }

}
