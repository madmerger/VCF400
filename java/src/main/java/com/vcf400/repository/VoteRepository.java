package com.vcf400.repository;

import com.vcf400.domain.Vote;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class VoteRepository {
    private static final RowMapper<Vote> MAPPER = (rs, i) -> new Vote(
            rs.getInt("BADGENBR"), rs.getInt("AWARDNBR"), rs.getString("EXHBNBR").trim());

    private final JdbcTemplate jdbc;

    public VoteRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** CHAIN VOTINGREC (キー = BADGENBR)。 */
    public Optional<Vote> findByBadge(int badgenbr) {
        return jdbc.query("SELECT * FROM VOTINGDB WHERE BADGENBR = ?", MAPPER, badgenbr).stream().findFirst();
    }

    /** WRITE VOTINGREC。 */
    public void insert(Vote v) {
        jdbc.update("INSERT INTO VOTINGDB (BADGENBR, AWARDNBR, EXHBNBR) VALUES (?, ?, ?)",
                v.badgenbr(), v.awardnbr(), v.exhbnbr());
    }

    public List<Vote> findAll() {
        return jdbc.query("SELECT * FROM VOTINGDB ORDER BY BADGENBR", MAPPER);
    }

    public int deleteByBadge(int badgenbr) {
        return jdbc.update("DELETE FROM VOTINGDB WHERE BADGENBR = ?", badgenbr);
    }
}
