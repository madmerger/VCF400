package com.vcf400.web.ui;

import com.vcf400.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class Vcf400UiController {
    private final ExhibitMenuService menu; private final Learn400Service learn; private final BeeMovieService bee; private final StaticScreenService screens;
    public Vcf400UiController(ExhibitMenuService menu, Learn400Service learn, BeeMovieService bee, StaticScreenService screens) { this.menu=menu;this.learn=learn;this.bee=bee;this.screens=screens; }
    @GetMapping("/") public String home(Model model) { model.addAttribute("title","VCF/400"); return "home"; }
    @GetMapping("/menu") public String menu(@RequestParam(defaultValue="GERTIE") String profile,Model model) { model.addAttribute("menu",menu.load(profile));model.addAttribute("profile",profile);return "menu"; }
    @GetMapping("/vote") public String vote(@RequestParam(defaultValue="GERTIE") String profile,@RequestParam(defaultValue="0") int help,Model model) { model.addAttribute("profile",profile);model.addAttribute("help",help==1?screens.help():null);return "vote"; }
    @GetMapping("/guestbook/add") public String addGuestbook(@RequestParam(defaultValue="GERTIE") String profile,Model model) { model.addAttribute("profile",profile);return "guestbook-add"; }
    @GetMapping("/guestbook/read") public String readGuestbook(@RequestParam(defaultValue="GERTIE") String profile,Model model) { model.addAttribute("profile",profile);return "guestbook-read"; }
    @GetMapping("/learn400") public String learn(@RequestParam(defaultValue="LRN400STR") String owner,@RequestParam(defaultValue="1") int page,Model model) {
        Learn400Service.Learn400Session session=learn.start(owner);
        for(int i=1;i<page;i++) session=learn.pageFwd(session);
        model.addAttribute("session",session); return "learn400";
    }
    @GetMapping("/learn400/auto") public String auto(Model model) { model.addAttribute("auto",true);return "learn400-auto"; }
    @GetMapping("/admin") public String admin(Model model) { model.addAttribute("title","ADMMAIN");return "admin"; }
    @GetMapping("/admin/beemovie") public String movie(Model model) { model.addAttribute("lines",bee.all());return "beemovie"; }
    @GetMapping("/credits") public String credits(Model model) { model.addAllAttributes(screens.credits());return "static"; }
}
