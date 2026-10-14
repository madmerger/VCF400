package com.vcf400.repository;

import com.vcf400.domain.Award;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AwardRepository {
    private static final RowMapper<Award> MAPPER = (rs, i) -> new Award(
            rs.getInt("AWARDID"), rs.getString("AWARDTITLE").trim(), rs.getString("AWARDDESC").trim());

    private final JdbcTemplate jdbc;

    public AwardRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** CHAIN AWARDRCD。 */
    public Optional<Award> findById(int awardid) {
        return jdbc.query("SELECT * FROM AWARDDB WHERE AWARDID = ?", MAPPER, awardid).stream().findFirst();
    }

    public List<Award> findAll() {
        return jdbc.query("SELECT * FROM AWARDDB ORDER BY AWARDID", MAPPER);
    }
}
