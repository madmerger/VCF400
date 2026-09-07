package com.vcf400.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class DataSeederTest {
    @Test
    void seedsOnlyOnceAndOverridesAdminPassword() throws Exception {
        String url = "jdbc:h2:mem:seedtest;MODE=DB2;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1";
        DataSource dataSource = dataSource(url);
        applySchema(dataSource);
        DataSeeder seeder = new DataSeeder(dataSource, url, "TESTPW");

        seeder.run(new DefaultApplicationArguments());
        seeder.run(new DefaultApplicationArguments());

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SETTINGS", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "SELECT VALUE FROM SETTINGS WHERE SETTING='ADMPSWRD'", String.class).trim())
            .isEqualTo("TESTPW");
    }

    @Test
    void failsWhenDatabaseIsPartiallyInitialized() throws Exception {
        String url = "jdbc:h2:mem:seedpartial;MODE=DB2;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1";
        DataSource dataSource = dataSource(url);
        applySchema(dataSource);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("INSERT INTO AWARDDB (AWARDID, AWARDTITLE, AWARDDESC) VALUES (1, 'existing', 'existing')");

        assertThatThrownBy(() -> new DataSeeder(dataSource, url, "TESTPW")
                .run(new DefaultApplicationArguments()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("seed failed: database is partially initialized (SETTINGS empty)");

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM AWARDDB", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SETTINGS", Integer.class)).isZero();
    }

    @Test
    void skipsWhenAnotherInstanceAlreadySeededSettings() throws Exception {
        String url = "jdbc:h2:mem:seedconcurrent;MODE=DB2;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1";
        DataSource dataSource = dataSource(url);
        applySchema(dataSource);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("INSERT INTO SETTINGS (SETTING, VALUE) VALUES ('ADMPSWRD', 'OTHERPW'), ('ALWVOTE', 'Y')");

        new DataSeeder(dataSource, url, "TESTPW").run(new DefaultApplicationArguments());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SETTINGS", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "SELECT VALUE FROM SETTINGS WHERE SETTING='ADMPSWRD'", String.class).trim())
            .isEqualTo("OTHERPW");
    }

    private static DataSource dataSource(String url) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private static void applySchema(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
        }
    }
}
