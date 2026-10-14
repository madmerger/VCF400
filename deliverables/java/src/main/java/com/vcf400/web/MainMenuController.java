package com.vcf400.web;

import com.vcf400.service.Messages;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** VCFMAIN メニュー (F-01, L-01) と NTRSTIT "How to Navigate" (F-02, L-02)。 */
@Controller
public class MainMenuController {

    private final String defaultProfile;

    public MainMenuController(@Value("${vcf.default-profile}") String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/menu";
    }

    /** GO VCFMAIN。 */
    @GetMapping("/menu")
    public String menu(HttpSession session, Model model, @RequestParam(required = false) String msg) {
        Nav.setReturnTo(session, Nav.MENU);
        model.addAttribute("launch", Nav.launch(session, defaultProfile));
        model.addAttribute("msg", msg);
        return "vcfmain";
    }

    /** メニュー番号入力 (VCFMAINQQ.mnucmd: 1, 11, 12, 13, 80)。 */
    @PostMapping("/menu")
    public String select(@RequestParam(defaultValue = "") String option, HttpSession session,
                         RedirectAttributes ra) {
        Nav.setReturnTo(session, Nav.MENU);
        String opt = option.trim().replaceFirst("^0+(?=\\d)", "");
        switch (opt) {
            case "1": return "redirect:/learn";
            case "11": return "redirect:/navigate?next=/vote";
            case "12": return "redirect:/navigate?next=/guestbook/add";
            case "13": return "redirect:/navigate?next=/guestbook/read";
            case "80": return "redirect:/signoff";
            case "": return "redirect:/menu";
            default:
                ra.addAttribute("msg", isMenuNumber(opt)
                        ? Messages.MSG_MENU_NOT_IN_LIB.formatted(opt)
                        : Messages.MSG_MENU_INVALID);
                return "redirect:/menu";
        }
    }

    private static boolean isMenuNumber(String opt) {
        return opt.matches("\\d{1,2}") && (Integer.parseInt(opt) <= 10 || opt.equals("90"));
    }

    /** サインオンユーザーの切替 (IBM i の SIGNON に相当。MM2024 で共用端末モード)。 */
    @PostMapping("/signon")
    public String signon(@RequestParam String profile, HttpSession session) {
        Nav.setLaunch(session, profile.isBlank() ? defaultProfile : profile);
        return "redirect:/menu";
    }

    @GetMapping("/signoff")
    public String signoff(HttpSession session) {
        session.invalidate();
        return "signoff";
    }

    /** NTRSTIT: VCF/400 - How to Navigate。ENTER で next へ。 */
    @GetMapping("/navigate")
    public String navigate(@RequestParam(defaultValue = "/menu") String next, Model model) {
        model.addAttribute("next", safeNext(next));
        return "navigate";
    }

    @PostMapping("/navigate")
    public String navigateContinue(@RequestParam(defaultValue = "/menu") String next) {
        return "redirect:" + safeNext(next);
    }

    private static String safeNext(String next) {
        return next != null && next.startsWith("/") && !next.startsWith("//") && !next.startsWith("/\\")
                ? next : "/menu";
    }
}
