package com.vcf400.repository;

import com.vcf400.domain.GuestbookComment;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class GuestbookRepository {
    private static final RowMapper<GuestbookComment> MAPPER = (rs, i) -> new GuestbookComment(
            rs.getInt("CMTID"), rs.getString("VISIBLE").trim(), rs.getString("EXHBID").trim(),
            rs.getString("GUESTNAME").trim(), rs.getString("GUESTCMT").trim());

    private final JdbcTemplate jdbc;

    public GuestbookRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** 末尾まで READ したときの最終レコード (採番・総数表示・B-09 のフォールバックに使う)。 */
    public Optional<GuestbookComment> findLast() {
        return jdbc.query("SELECT * FROM GUESTBKDB ORDER BY CMTID DESC FETCH FIRST 1 ROWS ONLY", MAPPER)
                .stream().findFirst();
    }

    /** SETLL + READ: キー以上の最初のレコード (B-09)。 */
    public Optional<GuestbookComment> findFirstAtOrAfter(int cmtid) {
        return jdbc.query("SELECT * FROM GUESTBKDB WHERE CMTID >= ? ORDER BY CMTID FETCH FIRST 1 ROWS ONLY",
                MAPPER, cmtid).stream().findFirst();
    }

    /** WRITE GUESTBKRCD。 */
    public void insert(GuestbookComment c) {
        jdbc.update("INSERT INTO GUESTBKDB (CMTID, VISIBLE, EXHBID, GUESTNAME, GUESTCMT) VALUES (?, ?, ?, ?, ?)",
                c.cmtid(), c.visible(), c.exhbid(), c.guestname(), c.guestcmt());
    }

    public List<GuestbookComment> findAll() {
        return jdbc.query("SELECT * FROM GUESTBKDB ORDER BY CMTID", MAPPER);
    }

    public int updateVisible(int cmtid, String visible) {
        return jdbc.update("UPDATE GUESTBKDB SET VISIBLE = ? WHERE CMTID = ?", visible, cmtid);
    }

    public int deleteById(int cmtid) {
        return jdbc.update("DELETE FROM GUESTBKDB WHERE CMTID = ?", cmtid);
    }
}
