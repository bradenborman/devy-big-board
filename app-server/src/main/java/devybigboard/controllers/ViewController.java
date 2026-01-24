package devybigboard.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/draft/*", "/league-filters"})
    public String view() {
        return "forward:/index.html";
    }

}