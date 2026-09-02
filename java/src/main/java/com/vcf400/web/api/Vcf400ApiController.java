package com.vcf400.web.api;

import com.vcf400.domain.*;
import com.vcf400.print.PrintSpoolService;
import com.vcf400.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Vcf400ApiController {
    private final AddVoteService vote; private final AddGuestbookCommentService addComment; private final ReadGuestbookCommentService readComment;
    private final ExhibitMenuService menu; private final AdminVoteReportService report; private final AdminExhibitService exhibit;
    private final AdminHideCommentService hide; private final AdminSecurityOfficerService officer; private final AdminSettingService setting;
    private final BeeMovieService bee; private final PrintSpoolService spool; private final Learn400Service learn; private final Learn400AutoService auto; private final StaticScreenService screens;
    public Vcf400ApiController(AddVoteService vote, AddGuestbookCommentService addComment, ReadGuestbookCommentService readComment, ExhibitMenuService menu, AdminVoteReportService report, AdminExhibitService exhibit, AdminHideCommentService hide, AdminSecurityOfficerService officer, AdminSettingService setting, BeeMovieService bee, PrintSpoolService spool, Learn400Service learn, Learn400AutoService auto, StaticScreenService screens) {
        this.vote=vote;this.addComment=addComment;this.readComment=readComment;this.menu=menu;this.report=report;this.exhibit=exhibit;this.hide=hide;this.officer=officer;this.setting=setting;this.bee=bee;this.spool=spool;this.learn=learn;this.auto=auto;this.screens=screens;
    }
    public record VoteRequest(@Min(0) int badge,@NotBlank String exhibitId,@Min(0) int award,String profile) {}
    @PostMapping("/vote") public AddVoteService.VoteResult vote(@Valid @RequestBody VoteRequest r) { return vote.submit(r.profile(),r.badge(),r.exhibitId(),r.award()); }
    public record CommentRequest(String profile,String exhibitId,String name,String comment) {}
    @PostMapping("/guestbook") public AddGuestbookCommentService.CommentResult comment(@RequestBody CommentRequest r) { return addComment.submit(r.profile(),r.exhibitId(),r.name(),r.comment()); }
    @GetMapping("/guestbook/{id}") public ReadGuestbookCommentService.CommentView comment(@PathVariable int id,@RequestParam(defaultValue="") String profile) { return readComment.read(profile,id); }
    @GetMapping("/learn400/{owner}") public Learn400Service.Learn400Session learn(@PathVariable String owner) { return learn.start(owner); }
    @GetMapping("/learn400/{owner}/fwd") public Learn400Service.Learn400Session learnFwd(@PathVariable String owner,@RequestParam(defaultValue="1") int page) { return move(learn.start(owner),page, true); }
    @GetMapping("/learn400/{owner}/back") public Learn400Service.Learn400Session learnBack(@PathVariable String owner,@RequestParam(defaultValue="1") int page) { return move(learn.start(owner),page, false); }
    private Learn400Service.Learn400Session move(Learn400Service.Learn400Session s,int page,boolean forward) { for(int i=1;i<page;i++) s=forward?learn.pageFwd(s):learn.pageBack(s); return s; }
    @GetMapping("/menu/{profile}") public ExhibitMenuService.MenuView menu(@PathVariable String profile) { return menu.load(profile); }
    @GetMapping("/menu/{profile}/select/{option}") public String select(@PathVariable String profile,@PathVariable int option) { return menu.select(profile,option); }
    @PostMapping("/menu/exit") public boolean exit(@RequestBody Map<String,String> body) { return menu.exitKiosk(body.get("password")); }
    @GetMapping("/admin/votes/{profile}") public AdminVoteReportService.VoteReport votes(@PathVariable String profile) { return report.readVote(profile); }
    @GetMapping("/admin/exhibits/{profile}") public Exhibit getExhibit(@PathVariable String profile) { return exhibit.load(profile); }
    @GetMapping("/admin/exhibits") public Object getExhibits() { return exhibit.list(); }
    @PostMapping("/admin/exhibits") public AdminExhibitService.ExhibitResult saveExhibit(@RequestBody AdminExhibitService.ExhibitForm form) { return exhibit.writeExhb(form); }
    @DeleteMapping("/admin/exhibits/{profile}") public AdminExhibitService.ExhibitResult deleteExhibit(@PathVariable String profile,@RequestParam(defaultValue="false") boolean confirmed) { return exhibit.delRcd(profile,confirmed); }
    @PostMapping("/admin/comments/{id}/visibility") public AdminHideCommentService.HideResult visibility(@PathVariable int id,@RequestBody Map<String,String> body) { return hide.update(id,body.get("visible")); }
    @PostMapping("/admin/officers") public String addOfficer(@RequestBody Map<String,String> body) { return officer.add(body.get("profile")); }
    @GetMapping("/admin/officers") public Object officers() { return officer.list(); }
    @GetMapping("/admin/settings") public Map<String,String> settings() { return setting.load(); }
    @PostMapping("/admin/settings") public Map<String,String> settings(@RequestBody Map<String,String> body) { return setting.save(body.get("password"),body.get("allowVote")); }
    @GetMapping("/beemovie") public Object beemovie() { return bee.all(); }
    @GetMapping("/print/spool") public Object printSpool() { return spool.spool(); }
    @GetMapping("/static/credits") public Map<String,String> credits() { return screens.credits(); }
    @GetMapping("/static/help") public Map<String,String> help() { return screens.help(); }
}
