package com.vcf400.service;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.Launch;
import com.vcf400.repository.ExhibitRepository;
import com.vcf400.repository.GuestbookRepository;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** ADDGBCMT.rpgle / READGBCMT.rpgle の移植 (F-04, F-05, V-10..V-18, B-06..B-09)。 */
@Service
public class GuestbookService {

    public record AddResult(String errLine, boolean exhibitErr, boolean nameErr, boolean commentErr,
                            GuestbookComment added) {
        public boolean hasError() { return errLine != null; }
    }

    /** READCMT の出力域 (OUTNAME 11 桁 / OUTTITLE 50 桁 / OUTCMT 200 桁)。 */
    public record ReadResult(String outName, String outTitle, String outCmt, GuestbookComment record) {}

    private final GuestbookRepository comments;
    private final ExhibitRepository exhibits;
    private final TransactionTemplate transactionTemplate;

    public GuestbookService(GuestbookRepository comments, ExhibitRepository exhibits,
                            TransactionTemplate transactionTemplate) {
        this.comments = comments;
        this.exhibits = exhibits;
        this.transactionTemplate = transactionTemplate;
    }

    public String protectedExhibit(Launch launch) {
        return launch.isShared() ? "" : launch.profile();
    }

    /** ADDCMT 送信: 必須検査 (展示 ID → 名前 → コメント) → ADDTODB (最終 CMTID + 1, VISIBLE='Y')。 */
    public AddResult add(Launch launch, String inName, String inId, String inCmt) {
        String id = launch.isShared() ? nz(inId).trim().toUpperCase() : launch.profile();
        String name = nz(inName).trim();
        String cmt = nz(inCmt).trim();

        String errLine = null;
        boolean in40 = false, in41 = false, in42 = false;
        int validate = 0;
        if (id.isEmpty()) { errLine = Messages.ERRBLKEX; in40 = true; } else { validate++; }
        if (name.isEmpty()) { errLine = Messages.ERRNONAME; in41 = true; } else { validate++; }
        if (cmt.isEmpty()) { errLine = Messages.ERRNOCMT; in42 = true; } else { validate++; }
        if (validate != 3) {
            return new AddResult(errLine, in40, in41, in42, null);
        }
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return transactionTemplate.execute(status -> {
                    int newId = comments.findLast().map(GuestbookComment::cmtid).orElse(0) + 1;   // B-06
                    GuestbookComment c = new GuestbookComment(newId, "Y", id, left(name, 20), left(cmt, 200));
                    comments.insert(c);
                    return new AddResult(null, false, false, false, c);
                });
            } catch (DuplicateKeyException e) {
                if (attempt == 2) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Comment insert retry loop exhausted");
    }

    /** GETTLCMT: "Currently hosting nnnn comments" = 最終レコードの CMTID。 */
    public int totalComments() {
        return comments.findLast().map(GuestbookComment::cmtid).orElse(0);
    }

    /** READDB: SETLL/READ による取得 (B-09) と可視性 (B-07)・帰属 (B-08) の置換。 */
    public Optional<ReadResult> read(Launch launch, int inCmtId) {
        Optional<GuestbookComment> rec = comments.findFirstAtOrAfter(inCmtId).or(comments::findLast);
        if (rec.isEmpty()) {
            return Optional.empty();
        }
        GuestbookComment c = rec.get();
        if (c.isHidden()) {
            return Optional.of(new ReadResult(Messages.ERRHNAME, "", Messages.ERRHCMT, c));
        }
        if (c.exhbid().equals(launch.profile()) || launch.isShared()) {
            String title = exhibits.findFirstAtOrAfter(c.exhbid()).map(e -> e.exhbtitle()).orElse("");
            return Optional.of(new ReadResult(left(c.guestname(), 11), title, c.guestcmt(), c));
        }
        return Optional.of(new ReadResult("", "", Messages.ERRPRIV, c));
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String left(String s, int n) { return s.length() <= n ? s : s.substring(0, n); }
}
