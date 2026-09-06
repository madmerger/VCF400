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

    /** CHAIN LRN400RCD。 */
    public Optional<LearnPage> findByPage(int pagenbr) {
        return jdbc.query("SELECT * FROM LRN400STR WHERE PAGENBR = ?", MAPPER, pagenbr).stream().findFirst();
    }

    /** ファイル先頭の READ。 */
    public Optional<LearnPage> findFirst() {
        return jdbc.query("SELECT * FROM LRN400STR ORDER BY PAGENBR FETCH FIRST 1 ROWS ONLY", MAPPER)
                .stream().findFirst();
    }

    public List<LearnPage> findAll() {
        return jdbc.query("SELECT * FROM LRN400STR ORDER BY PAGENBR", MAPPER);
    }
}
