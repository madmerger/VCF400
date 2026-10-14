package com.vcf400.web;

import com.vcf400.domain.Launch;
import com.vcf400.service.GuestbookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** ADDGBCMT / READGBCMT: GUESTBOOK/400 (F-04, F-05, L-07..L-09)。 */
@Controller
@RequestMapping("/guestbook")
public class GuestbookController {

    private final GuestbookService guestbook;
    private final String defaultProfile;

    public GuestbookController(GuestbookService guestbook, @Value("${vcf.default-profile}") String defaultProfile) {
        this.guestbook = guestbook;
        this.defaultProfile = defaultProfile;
    }

    // ---- ADD COMMENT -------------------------------------------------------

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        Launch launch = Nav.launch(session, defaultProfile);
        fillAdd(model, launch, "", guestbook.protectedExhibit(launch), "", null);
        return "addcmt";
    }

    @PostMapping("/add")
    public String add(@RequestParam(defaultValue = "submit") String action,
                      @RequestParam(defaultValue = "") String inName,
                      @RequestParam(defaultValue = "") String inId,
                      @RequestParam(defaultValue = "") String inCmt,
                      HttpSession session, Model model) {
        if ("cancel".equals(action)) {
            return "redirect:" + Nav.returnTo(session);
        }
        Launch launch = Nav.launch(session, defaultProfile);
        GuestbookService.AddResult r = guestbook.add(launch, inName, inId, inCmt);
        if (!r.hasError()) {
            model.addAttribute("added", r.added());
            model.addAttribute("returnTo", Nav.returnTo(session));
            return "endcmt";
        }
        fillAdd(model, launch, inName, launch.isShared() ? inId : guestbook.protectedExhibit(launch), inCmt, r);
        return "addcmt";
    }

    private void fillAdd(Model model, Launch launch, String name, String id, String cmt,
                         GuestbookService.AddResult r) {
        model.addAttribute("launch", launch);
        model.addAttribute("exhibitProtected", !launch.isShared());
        model.addAttribute("inName", name);
        model.addAttribute("inId", id);
        model.addAttribute("inCmt", cmt);
        model.addAttribute("result", r);
    }

    // ---- READ A COMMENT ----------------------------------------------------

    @GetMapping("/read")
    public String readForm(HttpSession session, Model model) {
        fillRead(model, Nav.launch(session, defaultProfile), "", null, null);
        return "readcmt";
    }

    @PostMapping("/read")
    public String read(@RequestParam(defaultValue = "submit") String action,
                       @RequestParam(defaultValue = "") String inCmtId,
                       HttpSession session, Model model) {
        if ("cancel".equals(action)) {
            return "redirect:" + Nav.returnTo(session);
        }
        Launch launch = Nav.launch(session, defaultProfile);
        int id = VoteController.numeric(inCmtId, 4);
        if (id == 0) {
            fillRead(model, launch, inCmtId, com.vcf400.service.Messages.ERRCMTID, null);   // V-14
            return "readcmt";
        }
        fillRead(model, launch, inCmtId, null, guestbook.read(launch, id).orElse(null));
        return "readcmt";
    }

    private void fillRead(Model model, Launch launch, String inCmtId, String errLine,
                          GuestbookService.ReadResult out) {
        model.addAttribute("launch", launch);
        model.addAttribute("inCmtId", inCmtId);
        model.addAttribute("errLine", errLine);
        model.addAttribute("total", guestbook.totalComments());
        model.addAttribute("out", out);
    }
}
