package com.vcf400.service;

import com.vcf400.domain.Award;
import com.vcf400.domain.Exhibit;
import com.vcf400.domain.Launch;
import com.vcf400.domain.Setting;
import com.vcf400.domain.Vote;
import com.vcf400.repository.AwardRepository;
import com.vcf400.repository.ExhibitRepository;
import com.vcf400.repository.SettingsRepository;
import com.vcf400.repository.VoteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ADDVOTE.rpgle の移植 (F-03, V-01..V-09, B-01..B-05)。 */
@Service
public class VoteService {

    /** VOTE1 画面 1 回の送信結果。errLine は「最後に失敗した検査」のメッセージ (B-05)。 */
    public record Result(String errLine, boolean badgeErr, boolean exhibitErr, boolean awardErr,
                         boolean recorded, Vote vote) {
        public boolean hasError() { return errLine != null; }
    }

    private final VoteRepository votes;
    private final ExhibitRepository exhibits;
    private final AwardRepository awards;
    private final SettingsRepository settings;

    public VoteService(VoteRepository votes, ExhibitRepository exhibits, AwardRepository awards,
                       SettingsRepository settings) {
        this.votes = votes;
        this.exhibits = exhibits;
        this.awards = awards;
        this.settings = settings;
    }

    /** CHKALWVOTE: SETTINGS.ALWVOTE = 'N' なら投票不可 (B-03)。 */
    public boolean isVotingAllowed() {
        return settings.find(Setting.ALWVOTE).map(s -> !"N".equals(s.value())).orElse(true);
    }

    /** CHKPARM: 通常起動では展示 ID は LAUNCH に固定・保護 (B-13)。 */
    public String protectedExhibit(Launch launch) {
        return launch.isShared() ? "" : launch.profile();
    }

    public List<Award> availableAwards() {
        return awards.findAll();
    }

    /**
     * VOTE1 の送信処理。
     * 1) 必須検査 (バッジ → アワード → 展示 ID の順、VALIDATE=3 で通過)
     * 2) ADDTODB (重複 → 資格 → 展示存在 → アワード存在、CHECKOK=4 で WRITE)
     */
    @Transactional
    public Result submit(Launch launch, int inputBadge, String inputExhibit, int inputAward) {
        String inexhb = launch.isShared() ? nz(inputExhibit).trim().toUpperCase() : launch.profile();

        String errLine = null;
        boolean in40 = false, in41 = false, in42 = false;
        int validate = 0;
        if (inputBadge == 0) { errLine = Messages.ERRBLKBG; in40 = true; } else { validate++; }
        if (inputAward == 0) { errLine = Messages.ERRBLKAW; in42 = true; } else { validate++; }
        if (inexhb.isEmpty()) { errLine = Messages.ERRBLKEX; in41 = true; } else { validate++; }
        if (validate != 3) {
            return new Result(errLine, in40, in41, in42, false, null);
        }

        // ADDTODB
        int checkOk = 0;
        if (votes.findByBadge(inputBadge).isPresent()) {
            errLine = Messages.ERREXIST;
        } else {
            checkOk++;
        }
        // SETLL/READ EXHBDB: キー以上の最初のレコードの ELIGIBLE を見る (レガシー準拠)
        Optional<Exhibit> atOrAfter = exhibits.findFirstAtOrAfter(inexhb);
        if (atOrAfter.isPresent() && !atOrAfter.get().isEligible()) {
            errLine = Messages.ERRPROHB;
        } else {
            checkOk++;
        }
        if (exhibits.findById(inexhb).isEmpty()) {
            errLine = Messages.ERRNOEXB;
        } else {
            checkOk++;
        }
        if (awards.findById(inputAward).isEmpty()) {
            errLine = Messages.ERRNOAWD;
        } else {
            checkOk++;
        }
        if (checkOk == 4) {
            Vote v = new Vote(inputBadge, inputAward, inexhb);
            try {
                votes.insert(v);        // WRITE VOTINGREC (+ PRTLSTVOTE 相当の監査は DB 一覧で代替)
            } catch (DuplicateKeyException e) {
                return new Result(Messages.ERREXIST, false, false, false, false, null);
            }
            return new Result(null, false, false, false, true, v);
        }
        return new Result(errLine, false, false, false, false, null);
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
