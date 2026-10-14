package com.vcf400.repository;

import com.vcf400.domain.Exhibit;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ExhibitRepository {
    private static final RowMapper<Exhibit> MAPPER = (rs, i) -> new Exhibit(
            rs.getInt("EXHBDBID"), rs.getString("EXHUSRPRF").trim(), rs.getString("EXHBITOR").trim(),
            rs.getString("EXHBCITY").trim(), rs.getString("EXHBSTATE").trim(), rs.getString("EXHBTITLE").trim(),
            rs.getString("EXHBDESC").trim(), rs.getInt("ELIGIBLE"), rs.getInt("ENLRN400"));

    private final JdbcTemplate jdbc;

    public ExhibitRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** CHAIN EXHBREC (キー = EXHUSRPRF)。 */
    public Optional<Exhibit> findById(String exhusrprf) {
        return jdbc.query("SELECT * FROM EXHBDB WHERE EXHUSRPRF = ?", MAPPER, exhusrprf).stream().findFirst();
    }

    /** SETLL + READ: キー以上の最初のレコード (レガシーの資格判定はこの読み方をする)。 */
    public Optional<Exhibit> findFirstAtOrAfter(String exhusrprf) {
        return jdbc.query("SELECT * FROM EXHBDB WHERE EXHUSRPRF >= ? ORDER BY EXHUSRPRF FETCH FIRST 1 ROWS ONLY",
                MAPPER, exhusrprf).stream().findFirst();
    }

    public List<Exhibit> findAll() {
        return jdbc.query("SELECT * FROM EXHBDB ORDER BY EXHUSRPRF", MAPPER);
    }
}
