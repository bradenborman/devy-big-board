package devybigboard.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({
        "/", 
        "/offline-draft", 
        "/live-draft",
        "/live-draft/**",
        "/player-management",
        "/draft/**", 
        "/drafts/**",
        "/league-filters"
    })
    public String view() {
        return "forward:/index.html";
    }

}