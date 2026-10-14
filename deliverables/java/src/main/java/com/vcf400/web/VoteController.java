package com.vcf400.web;

import com.vcf400.domain.Launch;
import com.vcf400.service.VoteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** ADDVOTE: NOMINATE EXHIBIT FOR AWARD (F-03, L-03, L-04)。 */
@Controller
@RequestMapping("/vote")
public class VoteController {

    private final VoteService votes;
    private final String defaultProfile;

    public VoteController(VoteService votes, @Value("${vcf.default-profile}") String defaultProfile) {
        this.votes = votes;
        this.defaultProfile = defaultProfile;
    }

    @GetMapping
    public String show(HttpSession session, Model model) {
        Launch launch = Nav.launch(session, defaultProfile);
        if (!votes.isVotingAllowed()) {
            model.addAttribute("returnTo", Nav.returnTo(session));
            return "endofcon";                       // B-03
        }
        fill(model, launch, "", votes.protectedExhibit(launch), "", null);
        return "vote1";
    }

    @PostMapping
    public String submit(@RequestParam(defaultValue = "submit") String action,
                         @RequestParam(defaultValue = "") String inputBadge,
                         @RequestParam(defaultValue = "") String inExhb,
                         @RequestParam(defaultValue = "") String inputAward,
                         HttpSession session, Model model) {
        if ("cancel".equals(action)) {               // F12
            return "redirect:" + Nav.returnTo(session);
        }
        Launch launch = Nav.launch(session, defaultProfile);
        if (!votes.isVotingAllowed()) {
            model.addAttribute("returnTo", Nav.returnTo(session));
            return "endofcon";
        }
        VoteService.Result r = votes.submit(launch, numeric(inputBadge, 4), inExhb, numeric(inputAward, 3));
        if (r.recorded()) {
            model.addAttribute("vote", r.vote());
            model.addAttribute("returnTo", Nav.returnTo(session));
            return "voteend";
        }
        fill(model, launch, inputBadge, launch.isShared() ? inExhb : votes.protectedExhibit(launch), inputAward, r);
        return "vote1";
    }

    private void fill(Model model, Launch launch, String badge, String exhb, String award, VoteService.Result r) {
        model.addAttribute("launch", launch);
        model.addAttribute("exhibitProtected", !launch.isShared());
        model.addAttribute("inputBadge", badge);
        model.addAttribute("inExhb", exhb);
        model.addAttribute("inputAward", award);
        model.addAttribute("awards", votes.availableAwards());
        model.addAttribute("result", r);
    }

    /** DDS の数値フィールド (CHECK(RZ)): 数字以外・空は 0 として扱う。 */
    static int numeric(String s, int digits) {
        String d = s == null ? "" : s.trim();
        if (!d.matches("\\d{1," + digits + "}")) {
            return 0;
        }
        return Integer.parseInt(d);
    }
}
