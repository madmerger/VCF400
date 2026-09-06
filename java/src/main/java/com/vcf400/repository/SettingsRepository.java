package com.vcf400.repository;

import com.vcf400.domain.Setting;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SettingsRepository {
    private static final RowMapper<Setting> MAPPER = (rs, i) -> new Setting(
            rs.getString("SETTING").trim(), rs.getString("VALUE").trim());

    private final JdbcTemplate jdbc;

    public SettingsRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** SETLL *LOVAL + READ 全件 (キー順)。 */
    public List<Setting> findAll() {
        return jdbc.query("SELECT * FROM SETTINGS ORDER BY SETTING", MAPPER);
    }

    public Optional<Setting> find(String setting) {
        return jdbc.query("SELECT * FROM SETTINGS WHERE SETTING = ?", MAPPER, setting).stream().findFirst();
    }

    public int update(String setting, String value) {
        return jdbc.update("UPDATE SETTINGS SET VALUE = ? WHERE SETTING = ?", value, setting);
    }
}
