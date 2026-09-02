package com.vcf400.web.ui;

import com.vcf400.service.AddGuestbookCommentService;
import com.vcf400.service.AddVoteService;
import com.vcf400.service.AdminExhibitService;
import com.vcf400.service.AdminHideCommentService;
import com.vcf400.service.AdminLearn400Service;
import com.vcf400.service.AdminSecurityOfficerService;
import com.vcf400.service.AdminSettingService;
import com.vcf400.service.AdminVoteReportService;
import com.vcf400.service.BeeMovieService;
import com.vcf400.service.ExhibitMenuService;
import com.vcf400.service.Learn400AutoService;
import com.vcf400.service.Learn400Service;
import com.vcf400.service.ReadGuestbookCommentService;
import com.vcf400.service.StaticScreenService;
import com.vcf400.print.PrintSpoolService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Vcf400UiController {

    private static final String LEARN_SESSION = "learn400Session";
    private static final String AUTO_SESSION = "learn400AutoSession";

    private final AddVoteService voteService;
    private final AddGuestbookCommentService addCommentService;
    private final ReadGuestbookCommentService readCommentService;
    private final ExhibitMenuService menuService;
    private final AdminVoteReportService reportService;
    private final AdminExhibitService exhibitService;
    private final AdminHideCommentService hideService;
    private final AdminLearn400Service adminLearnService;
    private final AdminSecurityOfficerService officerService;
    private final AdminSettingService settingService;
    private final BeeMovieService beeMovieService;
    private final Learn400Service learnService;
    private final Learn400AutoService autoService;
    private final StaticScreenService screens;
    private final PrintSpoolService spoolService;

    public Vcf400UiController(
            AddVoteService voteService,
            AddGuestbookCommentService addCommentService,
            ReadGuestbookCommentService readCommentService,
            ExhibitMenuService menuService,
            AdminVoteReportService reportService,
            AdminExhibitService exhibitService,
            AdminHideCommentService hideService,
            AdminLearn400Service adminLearnService,
            AdminSecurityOfficerService officerService,
            AdminSettingService settingService,
            BeeMovieService beeMovieService,
            Learn400Service learnService,
            Learn400AutoService autoService,
            StaticScreenService screens,
            PrintSpoolService spoolService) {
        this.voteService = voteService;
        this.addCommentService = addCommentService;
        this.readCommentService = readCommentService;
        this.menuService = menuService;
        this.reportService = reportService;
        this.exhibitService = exhibitService;
        this.hideService = hideService;
        this.adminLearnService = adminLearnService;
        this.officerService = officerService;
        this.settingService = settingService;
        this.beeMovieService = beeMovieService;
        this.learnService = learnService;
        this.autoService = autoService;
        this.screens = screens;
        this.spoolService = spoolService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "GERTIE") String profile,
            Model model) {
        model.addAttribute("profile", profile);
        return "home";
    }

    @PostMapping("/")
    public String homePost(
            @RequestParam String profile,
            @RequestParam int option,
            Model model) {
        model.addAttribute("profile", profile);
        model.addAttribute("option", option);
        return switch (option) {
            case 1 -> "redirect:/learn400?owner=" + profile;
            case 11 -> "redirect:/help?next=/vote?profile=" + profile;
            case 12 -> "redirect:/help?next=/guestbook/add?profile=" + profile;
            case 13 -> "redirect:/help?next=/guestbook/read?profile=" + profile;
            case 80 -> "redirect:/menu?profile=" + profile;
            case 90 -> "redirect:/admin";
            default -> "home";
        };
    }

    @GetMapping("/menu")
    public String menu(
            @RequestParam(defaultValue = "GERTIE") String profile,
            @RequestParam(defaultValue = "0") int exit,
            Model model) {
        model.addAttribute("profile", profile);
        model.addAttribute("menu", menuService.load(profile));
        model.addAttribute("exit", exit == 1);
        return "menu";
    }

    @PostMapping("/menu")
    public String menuPost(
            @RequestParam String profile,
            @RequestParam int option,
            Model model) {
        String destination = menuService.select(profile, option);
        model.addAttribute("profile", profile);
        if ("VOTESTUB".equals(destination)) {
            return "redirect:/help?next=/vote?profile=" + profile;
        }
        if ("LRN400STUB".equals(destination)) {
            return "redirect:/learn400?owner=" + profile;
        }
        if ("ADDGBSTUB".equals(destination)) {
            return "redirect:/help?next=/guestbook/add?profile=" + profile;
        }
        if ("READGBSTUB".equals(destination)) {
            return "redirect:/help?next=/guestbook/read?profile=" + profile;
        }
        if ("ADMKIOSK".equals(destination)) {
            return "redirect:/menu?profile=" + profile + "&exit=1";
        }
        model.addAttribute("menu", menuService.load(profile));
        return "menu";
    }

    @PostMapping("/menu/exit")
    public String menuExit(
            @RequestParam String profile,
            @RequestParam String password,
            Model model) {
        model.addAttribute("profile", profile);
        model.addAttribute("menu", menuService.load(profile));
        model.addAttribute("exit", true);
        model.addAttribute("exitResult", menuService.exitKiosk(password));
        return "menu";
    }

    @GetMapping("/help")
    public String help(
            @RequestParam(defaultValue = "/") String next,
            Model model) {
        model.addAllAttributes(screens.help());
        model.addAttribute("next", next);
        return "help";
    }

    @GetMapping("/vote")
    public String vote(
            @RequestParam(defaultValue = "GERTIE") String profile,
            Model model) {
        model.addAttribute("profile", profile);
        model.addAttribute("awards", java.util.List.of(
                "001. Best in Show Award",
                "002. The Ed Fair Award"));
        return "vote";
    }

    @PostMapping("/vote")
    public String votePost(
            @RequestParam String profile,
            @RequestParam(defaultValue = "0") int badge,
            @RequestParam(defaultValue = "") String exhibitId,
            @RequestParam(defaultValue = "0") int award,
            Model model) {
        AddVoteService.VoteResult result =
                voteService.submit(profile, badge, exhibitId, award);
        model.addAttribute("profile", profile);
        model.addAttribute("result", result);
        model.addAttribute("awards", java.util.List.of(
                "001. Best in Show Award",
                "002. The Ed Fair Award"));
        return "vote";
    }

    @GetMapping("/guestbook/add")
    public String addGuestbook(
            @RequestParam(defaultValue = "GERTIE") String profile,
            Model model) {
        model.addAttribute("profile", profile);
        return "guestbook-add";
    }

    @PostMapping("/guestbook/add")
    public String addGuestbookPost(
            @RequestParam String profile,
            @RequestParam(defaultValue = "") String exhibitId,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String comment,
            Model model) {
        model.addAttribute("profile", profile);
        model.addAttribute("result", addCommentService.submit(
                profile,
                exhibitId,
                name,
                comment));
        return "guestbook-add";
    }

    @GetMapping("/guestbook/read")
    public String readGuestbook(
            @RequestParam(defaultValue = "GERTIE") String profile,
            Model model) {
        model.addAttribute("profile", profile);
        return "guestbook-read";
    }

    @PostMapping("/guestbook/read")
    public String readGuestbookPost(
            @RequestParam String profile,
            @RequestParam(defaultValue = "0") int commentId,
            Model model) {
        model.addAttribute("profile", profile);
        model.addAttribute("result", readCommentService.read(profile, commentId));
        return "guestbook-read";
    }

    @GetMapping("/learn400")
    public String learn(
            @RequestParam(defaultValue = "LRN400STR") String owner,
            HttpSession httpSession,
            Model model) {
        Learn400Service.Learn400Session session =
                (Learn400Service.Learn400Session) httpSession.getAttribute(
                        LEARN_SESSION);
        if (session == null || !owner.equals(session.getOwner())) {
            session = learnService.start(owner);
            httpSession.setAttribute(LEARN_SESSION, session);
        }
        model.addAttribute("session", session);
        return "learn400";
    }

    @PostMapping("/learn400")
    public String learnPost(
            @RequestParam String owner,
            @RequestParam String action,
            HttpSession httpSession,
            Model model) {
        Learn400Service.Learn400Session session =
                (Learn400Service.Learn400Session) httpSession.getAttribute(
                        LEARN_SESSION);
        if (session == null || !owner.equals(session.getOwner())) {
            session = learnService.start(owner);
        }
        if ("fwd".equals(action)) {
            session = learnService.pageFwd(session);
        }
        if ("back".equals(action)) {
            session = learnService.pageBack(session);
        }
        if ("exit".equals(action)) {
            httpSession.removeAttribute(LEARN_SESSION);
        } else {
            httpSession.setAttribute(LEARN_SESSION, session);
        }
        model.addAttribute("session", session);
        return "learn400";
    }

    @GetMapping("/learn400/auto")
    public String learnAuto(
            @RequestParam(defaultValue = "LRN400STR") String owner,
            HttpSession httpSession,
            Model model) {
        Learn400Service.Learn400Session session =
                autoService.start(owner);
        httpSession.setAttribute(AUTO_SESSION, session);
        model.addAttribute("session", session);
        return "learn400-auto";
    }

    @PostMapping("/learn400/auto")
    public String learnAutoPost(
            @RequestParam String owner,
            @RequestParam(defaultValue = "fwd") String action,
            HttpSession httpSession,
            Model model) {
        Learn400Service.Learn400Session session =
                (Learn400Service.Learn400Session) httpSession.getAttribute(
                        AUTO_SESSION);
        if (session == null) {
            session = autoService.start(owner);
        }
        if ("exit".equals(action)) {
            httpSession.removeAttribute(AUTO_SESSION);
        } else {
            session = autoService.pageFwd(session);
            httpSession.setAttribute(AUTO_SESSION, session);
        }
        model.addAttribute("session", session);
        return "learn400-auto";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/admin/votes")
    public String adminVotes(
            @RequestParam(defaultValue = "") String profile,
            Model model) {
        model.addAttribute("profile", profile);
        if (!profile.isBlank()) {
            model.addAttribute("report", reportService.readVote(profile));
        }
        return "admin-votes";
    }

    @PostMapping("/admin/votes")
    public String adminVotesPost(
            @RequestParam String profile,
            Model model) {
        return adminVotes(profile, model);
    }

    @GetMapping("/admin/exhibits")
    public String adminExhibits(
            @RequestParam(defaultValue = "NONE") String profile,
            Model model) {
        model.addAttribute("exhibits", exhibitService.list());
        model.addAttribute("editor", exhibitService.loadSession(profile));
        return "admin-exhibits";
    }

    @PostMapping("/admin/exhibits")
    public String adminExhibitsPost(
            @RequestParam String profile,
            @RequestParam(defaultValue = "") String owner,
            @RequestParam(defaultValue = "") String city,
            @RequestParam(defaultValue = "") String state,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "0") int eligible,
            @RequestParam(defaultValue = "0") int learn400,
            @RequestParam(defaultValue = "save") String action,
            @RequestParam(defaultValue = "false") boolean confirmed,
            Model model) {
        AdminExhibitService.ExhibitResult result;
        if ("delete".equals(action)) {
            result = exhibitService.delRcd(profile, confirmed);
            model.addAttribute("deleteConfirm", !confirmed && result.exhibit() != null);
        } else {
            result = exhibitService.writeExhb(new AdminExhibitService.ExhibitForm(
                    profile,
                    owner,
                    city,
                    state,
                    title,
                    description,
                    eligible,
                    learn400));
        }
        model.addAttribute("result", result);
        model.addAttribute("exhibits", exhibitService.list());
        model.addAttribute(
                "editor",
                exhibitService.loadSession(result.success() ? "NONE" : profile));
        return "admin-exhibits";
    }

    @GetMapping("/admin/comments")
    public String adminComments(Model model) {
        model.addAttribute("comments", hideService.list());
        return "admin-comments";
    }

    @PostMapping("/admin/comments")
    public String adminCommentsPost(
            @RequestParam int commentId,
            @RequestParam String visible,
            Model model) {
        model.addAttribute("result", hideService.update(commentId, visible));
        return adminComments(model);
    }

    @GetMapping("/admin/learn400")
    public String adminLearn400(Model model) {
        model.addAttribute("editor", adminLearnService.start("LRN400STR"));
        return "admin-learn400";
    }

    @PostMapping("/admin/learn400")
    public String adminLearn400Post(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String content,
            @RequestParam(defaultValue = "") String extra,
            @RequestParam(defaultValue = "save") String action,
            Model model) {
        AdminLearn400Service.AdminLearnSession editor =
                adminLearnService.start("LRN400STR");
        for (int index = 1; index < page; index++) {
            editor = adminLearnService.pageFwd(editor);
        }
        editor.setInContent(content);
        editor.setInExtra(extra);
        if ("fwd".equals(action)) {
            editor = adminLearnService.pageFwd(editor);
        } else if ("back".equals(action)) {
            editor = adminLearnService.pageBack(editor);
        } else {
            editor = adminLearnService.save(editor);
        }
        model.addAttribute("editor", editor);
        return "admin-learn400";
    }

    @GetMapping("/admin/officers")
    public String adminOfficers(Model model) {
        model.addAttribute("officers", officerService.list());
        return "admin-officers";
    }

    @PostMapping("/admin/officers")
    public String adminOfficersPost(
            @RequestParam String profile,
            Model model) {
        model.addAttribute("result", officerService.add(profile));
        return adminOfficers(model);
    }

    @GetMapping("/admin/settings")
    public String adminSettings(Model model) {
        model.addAttribute("settings", settingService.load());
        return "admin-settings";
    }

    @PostMapping("/admin/settings")
    public String adminSettingsPost(
            @RequestParam String password,
            @RequestParam String allowVote,
            Model model) {
        model.addAttribute(
                "settings",
                settingService.save(password, allowVote));
        return "admin-settings";
    }

    @GetMapping("/admin/beemovie")
    public String movie(Model model) {
        model.addAttribute("lines", beeMovieService.all());
        return "beemovie";
    }

    @GetMapping("/credits")
    public String credits(Model model) {
        model.addAllAttributes(screens.credits());
        return "static";
    }

    @GetMapping("/admin/spool")
    public String spool(Model model) {
        model.addAttribute("spool", spoolService.spool());
        return "admin-spool";
    }
}
