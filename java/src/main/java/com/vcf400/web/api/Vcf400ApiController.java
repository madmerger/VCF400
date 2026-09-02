package com.vcf400.web.api;

import java.util.List;
import java.util.Map;

import com.vcf400.domain.Exhibit;
import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.SecurityOfficer;
import com.vcf400.print.PrintSpoolService;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Vcf400ApiController {

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
    private final PrintSpoolService spoolService;
    private final Learn400Service learnService;
    private final Learn400AutoService autoService;
    private final StaticScreenService screens;

    public Vcf400ApiController(
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
            PrintSpoolService spoolService,
            Learn400Service learnService,
            Learn400AutoService autoService,
            StaticScreenService screens) {
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
        this.spoolService = spoolService;
        this.learnService = learnService;
        this.autoService = autoService;
        this.screens = screens;
    }

    public record VoteRequest(
            @Min(0) int badge,
            @NotBlank String exhibitId,
            @Min(0) int award,
            String profile) {
    }

    @PostMapping("/vote")
    public AddVoteService.VoteResult vote(
            @Valid @RequestBody VoteRequest request) {
        return voteService.submit(
                request.profile(),
                request.badge(),
                request.exhibitId(),
                request.award());
    }

    public record CommentRequest(
            String profile,
            String exhibitId,
            String name,
            String comment) {
    }

    @PostMapping("/guestbook")
    public AddGuestbookCommentService.CommentResult comment(
            @RequestBody CommentRequest request) {
        return addCommentService.submit(
                request.profile(),
                request.exhibitId(),
                request.name(),
                request.comment());
    }

    @GetMapping("/guestbook/{id}")
    public ReadGuestbookCommentService.CommentView comment(
            @PathVariable int id,
            @RequestParam(defaultValue = "") String profile) {
        return readCommentService.read(profile, id);
    }

    @GetMapping("/guestbook")
    public List<GuestbookComment> comments() {
        return hideService.list();
    }

    @GetMapping("/learn400/{owner}")
    public Learn400Service.Learn400Session learn(
            @PathVariable String owner) {
        return learnService.start(owner);
    }

    @GetMapping("/learn400/{owner}/fwd")
    public Learn400Service.Learn400Session learnFwd(
            @PathVariable String owner,
            @RequestParam(defaultValue = "1") int page) {
        return move(owner, page, true);
    }

    @GetMapping("/learn400/{owner}/back")
    public Learn400Service.Learn400Session learnBack(
            @PathVariable String owner,
            @RequestParam(defaultValue = "1") int page) {
        return move(owner, page, false);
    }

    @GetMapping("/learn400/auto/{owner}")
    public Learn400Service.Learn400Session learnAuto(
            @PathVariable String owner) {
        return autoService.start(owner);
    }

    private Learn400Service.Learn400Session move(
            String owner,
            int page,
            boolean forward) {
        Learn400Service.Learn400Session session = learnService.start(owner);
        for (int index = 1; index < page; index++) {
            session = forward
                    ? learnService.pageFwd(session)
                    : learnService.pageBack(session);
        }
        return session;
    }

    @GetMapping("/menu/{profile}")
    public ExhibitMenuService.MenuView menu(
            @PathVariable String profile) {
        return menuService.load(profile);
    }

    @GetMapping("/menu/{profile}/select/{option}")
    public String select(
            @PathVariable String profile,
            @PathVariable int option) {
        return menuService.select(profile, option);
    }

    @PostMapping("/menu/exit")
    public boolean exit(@RequestBody Map<String, String> body) {
        return menuService.exitKiosk(body.get("password"));
    }

    @GetMapping("/admin/votes/{profile}")
    public AdminVoteReportService.VoteReport votes(
            @PathVariable String profile) {
        return reportService.readVote(profile);
    }

    @GetMapping("/admin/exhibits/{profile}")
    public Exhibit getExhibit(@PathVariable String profile) {
        return exhibitService.load(profile);
    }

    @GetMapping("/admin/exhibits")
    public List<Exhibit> getExhibits() {
        return exhibitService.list();
    }

    @PostMapping("/admin/exhibits")
    public AdminExhibitService.ExhibitResult saveExhibit(
            @RequestBody AdminExhibitService.ExhibitForm form) {
        return exhibitService.writeExhb(form);
    }

    @DeleteMapping("/admin/exhibits/{profile}")
    public AdminExhibitService.ExhibitResult deleteExhibit(
            @PathVariable String profile,
            @RequestParam(defaultValue = "false") boolean confirmed) {
        return exhibitService.delRcd(profile, confirmed);
    }

    @PostMapping("/admin/comments/{id}/visibility")
    public AdminHideCommentService.HideResult visibility(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        return hideService.update(id, body.get("visible"));
    }

    @GetMapping("/admin/learn400")
    public AdminLearn400Service.AdminLearnSession adminLearn400() {
        return adminLearnService.start("LRN400STR");
    }

    @PostMapping("/admin/learn400")
    public AdminLearn400Service.AdminLearnSession adminLearn400Save(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String content,
            @RequestParam(defaultValue = "") String extra) {
        AdminLearn400Service.AdminLearnSession session =
                adminLearnService.start("LRN400STR");
        for (int index = 1; index < page; index++) {
            session = adminLearnService.pageFwd(session);
        }
        session.setInContent(content);
        session.setInExtra(extra);
        return adminLearnService.save(session);
    }

    @PostMapping("/admin/officers")
    public String addOfficer(@RequestBody Map<String, String> body) {
        return officerService.add(body.get("profile"));
    }

    @GetMapping("/admin/officers")
    public List<SecurityOfficer> officers() {
        return officerService.list();
    }

    @GetMapping("/admin/settings")
    public Map<String, String> settings() {
        return settingService.load();
    }

    @PostMapping("/admin/settings")
    public Map<String, String> settings(
            @RequestBody Map<String, String> body) {
        return settingService.save(
                body.get("password"),
                body.get("allowVote"));
    }

    @GetMapping("/beemovie")
    public Object beemovie() {
        return beeMovieService.all();
    }

    @GetMapping("/print/spool")
    public List<String> printSpool() {
        return spoolService.spool();
    }

    @GetMapping("/static/credits")
    public Map<String, String> credits() {
        return screens.credits();
    }

    @GetMapping("/static/help")
    public Map<String, String> help() {
        return screens.help();
    }
}
