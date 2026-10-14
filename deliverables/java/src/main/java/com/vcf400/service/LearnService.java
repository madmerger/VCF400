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

    /** OVRDBF なし (VCFMAIN 1) の共通ページファイル。 */
    public static final String COMMON_FILE = "LRN400STR";

    /** RPG のプログラム変数に対応する状態。lrnfile = OVRDBF で開いているページファイル。 */
    public record State(String lrnfile, int curPageNbr, int frmPageNbr, boolean alwFwd, String outPageNbr,
                        String outContent, boolean exit) {
        public State withExit() {
            return new State(lrnfile, curPageNbr, frmPageNbr, alwFwd, outPageNbr, outContent, true);
        }
    }

    private final LearnPageRepository pages;

    public LearnService(LearnPageRepository pages) { this.pages = pages; }

    /**
     * 初期表示: ファイル先頭ページ。展示別ファイル (LRN400STUB の OVRDBF 相当) にページが
     * なければ共通 LRN400STR にフォールバックする (レガシーは存在しないファイルで異常終了する;
     * 移行版では警告ログを出して共通ページを表示する仕様上の差異)。
     */
    public State start(String lrnfile) {
        String file = resolve(lrnfile);
        Optional<LearnPage> first = pages.findFirst(file);
        int nbr = first.map(LearnPage::pagenbr).orElse(0);
        return new State(file, nbr, 0, true, first.map(p -> String.valueOf(p.pagenbr())).orElse(""),
                first.map(LearnPage::content).orElse(""), false);
    }

    private String resolve(String lrnfile) {
        String file = lrnfile == null || lrnfile.isBlank() ? COMMON_FILE : lrnfile.trim().toUpperCase();
        if (!COMMON_FILE.equals(file) && pages.findFirst(file).isEmpty()) {
            log.warn("LRN400 ページファイル {} が空のため共通 {} を使用します", file, COMMON_FILE);
            return COMMON_FILE;
        }
        return file;
    }

    /** F5 = PAGEFWD。見つからなければ本文をクリアして ALWFWD=0。EXTRA='END' で終了 (JUMP/CALL は JUMPTO)。 */
    public State forward(State s) {
        String file = s.lrnfile();
        int cur = s.curPageNbr();
        int frm = s.frmPageNbr();
        if (s.alwFwd()) { cur += 1; frm = 0; }
        Optional<LearnPage> p = pages.findByPage(file, cur);
        if (p.isEmpty()) {
            return new State(file, cur, frm, false, s.outPageNbr(), "", false);
        }
        LearnPage page = p.get();
        if (page.isEnd()) {
            return new State(file, cur, frm, true, String.valueOf(page.pagenbr()), page.content(), true);
        }
        if ("CALL".equals(page.content())) {
            log.warn("LRN400 CALL {} は Java 版では未対応のため次ページへ進みます", page.extra());
            frm = cur - 1;
            cur += 1;
            Optional<LearnPage> target = pages.findByPage(file, cur);
            return new State(file, cur, frm, true, target.map(t -> String.valueOf(t.pagenbr())).orElse(""),
                    target.map(LearnPage::content).orElse(""), false);
        }
        if ("JUMP".equals(page.content())) {
            frm = cur - 1;
            cur = parseInt(page.extra());
            Optional<LearnPage> target = pages.findByPage(file, cur);
            return new State(file, cur, frm, true, target.map(t -> String.valueOf(t.pagenbr())).orElse(""),
                    target.map(LearnPage::content).orElse(""), false);
        }
        return new State(file, cur, frm, true, String.valueOf(page.pagenbr()), page.content(), false);
    }

    /** F8 = PAGEBACK。CURPAGENBR-1 (0 未満にしない)、FRMPAGENBR があればそこへ戻る。 */
    public State back(State s) {
        String file = s.lrnfile();
        int cur = s.curPageNbr() == 0 ? 0 : s.curPageNbr() - 1;
        int target = s.frmPageNbr() == 0 ? cur : s.frmPageNbr();
        Optional<LearnPage> p = pages.findByPage(file, target);
        if (p.isEmpty()) {
            // CHAIN 不一致 (例: ページ 1 で F8): レガシーは直前の表示を保つ
            return new State(file, cur, 0, true, s.outPageNbr(), s.outContent(), false);
        }
        return new State(file, cur, 0, true, String.valueOf(p.get().pagenbr()), p.get().content(), false);
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }
}
