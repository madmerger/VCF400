package com.vcf400.repository;

import com.vcf400.domain.LearnPage;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LearnPageRepository {
    private static final RowMapper<LearnPage> MAPPER = (rs, i) -> new LearnPage(
            rs.getInt("PAGENBR"), rs.getString("CONTENT").trim(), rs.getString("EXTRA").trim());

    private final JdbcTemplate jdbc;

    public LearnPageRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** CHAIN LRN400RCD (OVRDBF 後のファイル = lrnfile)。 */
    public Optional<LearnPage> findByPage(String lrnfile, int pagenbr) {
        return jdbc.query("SELECT * FROM LRN400STR WHERE LRNFILE = ? AND PAGENBR = ?",
                MAPPER, lrnfile, pagenbr).stream().findFirst();
    }

    /** ファイル先頭の READ。 */
    public Optional<LearnPage> findFirst(String lrnfile) {
        return jdbc.query("SELECT * FROM LRN400STR WHERE LRNFILE = ? ORDER BY PAGENBR FETCH FIRST 1 ROWS ONLY",
                MAPPER, lrnfile).stream().findFirst();
    }

    public List<LearnPage> findAll(String lrnfile) {
        return jdbc.query("SELECT * FROM LRN400STR WHERE LRNFILE = ? ORDER BY PAGENBR", MAPPER, lrnfile);
    }
}
