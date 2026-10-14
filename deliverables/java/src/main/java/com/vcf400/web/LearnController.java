package com.vcf400.web;

import com.vcf400.service.LearnService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** LRN400: LEARN/400 (F-06, L-10)。F3 Exit / F5 Forwards / F8 Backwards。 */
@Controller
@RequestMapping("/learn")
public class LearnController {
    private static final String STATE = "vcf.learnState";

    private final LearnService learn;

    public LearnController(LearnService learn) { this.learn = learn; }

    @GetMapping
    public String show(HttpSession session, Model model) {
        LearnService.State s = learn.start();
        session.setAttribute(STATE, s);
        model.addAttribute("state", s);
        return "lrn400";
    }

    @PostMapping
    public String key(@RequestParam(defaultValue = "") String action, HttpSession session, Model model) {
        Object v = session.getAttribute(STATE);
        LearnService.State s = v instanceof LearnService.State st ? st : learn.start();
        switch (action) {
            case "exit" -> {                                  // F3
                session.removeAttribute(STATE);
                return "redirect:" + Nav.returnTo(session);
            }
            case "fwd" -> s = learn.forward(s);               // F5
            case "back" -> s = learn.back(s);                 // F8
            default -> { }                                    // ENTER: 再表示
        }
        if (s.exit()) {                                       // EXTRA='END' (B-10)
            session.removeAttribute(STATE);
            return "redirect:" + Nav.returnTo(session);
        }
        session.setAttribute(STATE, s);
        model.addAttribute("state", s);
        return "lrn400";
    }
}
