package com.vcf400.web;

import com.vcf400.service.KioskService;
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
    private final KioskService kiosk;

    public LearnController(LearnService learn, KioskService kiosk) {
        this.learn = learn;
        this.kiosk = kiosk;
    }

    /**
     * GET /learn — VCFMAIN 1 は共通 LRN400STR、キオスクのオプション 2 は ?exhb=展示ID で
     * その展示のページファイル (LRN400STUB の OVRDBF 相当) を使う。ページは公開コンテンツのため
     * パラメータで受けてよいが、EXHBDB に存在し ENLRN400=1 の展示のみ有効 (それ以外は共通)。
     */
    @GetMapping
    public String show(@RequestParam(required = false) String exhb, HttpSession session, Model model) {
        String file = exhb != null
                && kiosk.exhibitFor(exhb).map(e -> e.isLearnEnabled()).orElse(false)
                ? exhb.trim().toUpperCase() : LearnService.COMMON_FILE;
        LearnService.State s = learn.start(file);
        session.setAttribute(STATE, s);
        model.addAttribute("state", s);
        return "lrn400";
    }

    @PostMapping
    public String key(@RequestParam(defaultValue = "") String action, HttpSession session, Model model) {
        Object v = session.getAttribute(STATE);
        LearnService.State s = v instanceof LearnService.State st ? st : learn.start(LearnService.COMMON_FILE);
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
