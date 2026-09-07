package com.vcf400.web;

import com.vcf400.domain.Exhibit;
import com.vcf400.service.KioskService;
import com.vcf400.service.Messages;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** EXHBMENU: 展示キオスク "WELCOME TO..." (F-07, L-05, L-06)。STREXHB EXHBNAME(x) = /kiosk/x。 */
@Controller
public class KioskController {

    private final KioskService kiosk;

    public KioskController(KioskService kiosk) { this.kiosk = kiosk; }

    @GetMapping("/kiosk/{exhibit}")
    public String menu(@PathVariable String exhibit, HttpSession session, Model model,
                       @RequestParam(required = false) String msg, RedirectAttributes ra) {
        String id = exhibit.trim().toUpperCase();
        Optional<Exhibit> e = kiosk.exhibitFor(id);
        if (e.isEmpty()) {
            ra.addAttribute("msg", Messages.MSG_KIOSK_NOT_FOUND.formatted(id));
            return "redirect:/menu";
        }
        // キオスク端末は展示者プロファイルでサインオンしている: LAUNCH = 展示 ID、終了後はこのメニューに戻る
        Nav.setLaunch(session, id);
        Nav.setReturnTo(session, "/kiosk/" + id);
        model.addAttribute("exhibit", e.get());
        model.addAttribute("msg", msg);
        return "kiosk";
    }

    @PostMapping("/kiosk/{exhibit}")
    public String select(@PathVariable String exhibit, @RequestParam(defaultValue = "") String option,
                         HttpSession session, RedirectAttributes ra) {
        String id = exhibit.trim().toUpperCase();
        Optional<Exhibit> e = kiosk.exhibitFor(id);
        if (e.isEmpty()) {
            return "redirect:/menu";
        }
        Nav.setLaunch(session, id);
        Nav.setReturnTo(session, "/kiosk/" + id);
        String base = "redirect:/kiosk/" + id;
        return switch (option.trim()) {
            case "1" -> e.get().isEligible() ? "redirect:/navigate?next=/vote" : base;             // B-02
            case "2" -> e.get().isLearnEnabled() ? "redirect:/learn" : base;                        // B-12
            case "3" -> "redirect:/navigate?next=/guestbook/add";
            case "4" -> "redirect:/navigate?next=/guestbook/read";
            case "7" -> base + "/exit";                                                             // B-11
            case "" -> { ra.addAttribute("msg", Messages.MSG_KIOSK_SELECT); yield base; }
            default -> base;                                                                        // 未定義番号: 再表示
        };
    }

    @GetMapping("/kiosk/{exhibit}/exit")
    public String exitPrompt(@PathVariable String exhibit, Model model) {
        model.addAttribute("exhibitId", exhibit.trim().toUpperCase());
        return "admpswrd";
    }

    @PostMapping("/kiosk/{exhibit}/exit")
    public String exit(@PathVariable String exhibit, @RequestParam(defaultValue = "") String inPwd,
                       HttpSession session, RedirectAttributes ra) {
        String id = exhibit.trim().toUpperCase();
        if (kiosk.exitAllowed(inPwd)) {
            session.removeAttribute(Nav.LAUNCH);
            Nav.setReturnTo(session, Nav.MENU);
            ra.addAttribute("msg", Messages.MSG_KIOSK_ENDED.formatted(id));
            return "redirect:/menu";
        }
        return "redirect:/kiosk/" + id;     // 不一致: キオスクメニュー再表示
    }
}
