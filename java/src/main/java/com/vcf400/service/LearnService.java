package com.vcf400.service;

import com.vcf400.domain.LearnPage;
import com.vcf400.repository.LearnPageRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** LRN400.rpgle の移植 (F-06, V-19..V-21, B-10)。状態 (CURPAGENBR 等) は呼び出し側 (セッション) が保持する。 */
@Service
public class LearnService {
    private static final Logger log = LoggerFactory.getLogger(LearnService.class);

    /** RPG のプログラム変数に対応する状態。 */
    public record State(int curPageNbr, int frmPageNbr, boolean alwFwd, String outPageNbr, String outContent,
                        boolean exit) {
        public State withExit() { return new State(curPageNbr, frmPageNbr, alwFwd, outPageNbr, outContent, true); }
    }

    private final LearnPageRepository pages;

    public LearnService(LearnPageRepository pages) { this.pages = pages; }

    /** 初期表示: ファイル先頭ページ。 */
    public State start() {
        Optional<LearnPage> first = pages.findFirst();
        int nbr = first.map(LearnPage::pagenbr).orElse(0);
        return new State(nbr, 0, true, first.map(p -> String.valueOf(p.pagenbr())).orElse(""),
                first.map(LearnPage::content).orElse(""), false);
    }

    /** F5 = PAGEFWD。見つからなければ本文をクリアして ALWFWD=0。EXTRA='END' で終了 (JUMP/CALL は JUMPTO)。 */
    public State forward(State s) {
        int cur = s.curPageNbr();
        int frm = s.frmPageNbr();
        if (s.alwFwd()) { cur += 1; frm = 0; }
        Optional<LearnPage> p = pages.findByPage(cur);
        if (p.isEmpty()) {
            return new State(cur, frm, false, s.outPageNbr(), "", false);
        }
        LearnPage page = p.get();
        if (page.isEnd()) {
            return new State(cur, frm, true, String.valueOf(page.pagenbr()), page.content(), true);
        }
        if ("CALL".equals(page.content())) {
            log.warn("LRN400 CALL {} は Java 版では未対応のため次ページへ進みます", page.extra());
            frm = cur - 1;
            cur += 1;
            Optional<LearnPage> target = pages.findByPage(cur);
            return new State(cur, frm, true, target.map(t -> String.valueOf(t.pagenbr())).orElse(""),
                    target.map(LearnPage::content).orElse(""), false);
        }
        if ("JUMP".equals(page.content())) {
            frm = cur - 1;
            cur = parseInt(page.extra());
            Optional<LearnPage> target = pages.findByPage(cur);
            return new State(cur, frm, true, target.map(t -> String.valueOf(t.pagenbr())).orElse(""),
                    target.map(LearnPage::content).orElse(""), false);
        }
        return new State(cur, frm, true, String.valueOf(page.pagenbr()), page.content(), false);
    }

    /** F8 = PAGEBACK。CURPAGENBR-1 (0 未満にしない)、FRMPAGENBR があればそこへ戻る。 */
    public State back(State s) {
        int cur = s.curPageNbr() == 0 ? 0 : s.curPageNbr() - 1;
        int target = s.frmPageNbr() == 0 ? cur : s.frmPageNbr();
        Optional<LearnPage> p = pages.findByPage(target);
        if (p.isEmpty()) {
            // CHAIN 不一致 (例: ページ 1 で F8): レガシーは直前の表示を保つ
            return new State(cur, 0, true, s.outPageNbr(), s.outContent(), false);
        }
        return new State(cur, 0, true, String.valueOf(p.get().pagenbr()), p.get().content(), false);
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }
}
