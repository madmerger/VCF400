package com.vcf400.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

    @Test
    void emptyAdminPasswordSeedsEmptyAdmpswrd() throws Exception {
        String url = "jdbc:h2:mem:seedemptypw;MODE=DB2;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1";
        DataSource dataSource = dataSource(url);
        applySchema(dataSource);

        new DataSeeder(dataSource, url, "").run(new DefaultApplicationArguments());

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject(
                "SELECT VALUE FROM SETTINGS WHERE SETTING='ADMPSWRD'", String.class).trim()).isEmpty();
    }

    @Test
    void appliesSchemaAndSeedsFreshFileDatabase(@TempDir Path dir) throws Exception {
        String url = "jdbc:h2:file:" + dir.resolve("vcf400")
                + ";MODE=DB2;NON_KEYWORDS=VALUE;DEFAULT_NULL_ORDERING=HIGH";
        DataSource dataSource = dataSource(url);
        DataSeeder seeder = new DataSeeder(dataSource, url, "TESTPW");

        seeder.run(new DefaultApplicationArguments());
        seeder.run(new DefaultApplicationArguments());

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SETTINGS", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM EXHBDB", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject(
                "SELECT VALUE FROM SETTINGS WHERE SETTING='ADMPSWRD'", String.class).trim())
            .isEqualTo("TESTPW");
    }

    @Test
    void upgradesOldLrn400strWithoutLrnfile(@TempDir Path dir) throws Exception {
        String url = "jdbc:h2:file:" + dir.resolve("vcf400old")
                + ";MODE=DB2;NON_KEYWORDS=VALUE;DEFAULT_NULL_ORDERING=HIGH";
        DataSource dataSource = dataSource(url);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        // 旧スキーマ (LRNFILE なし、PK = PAGENBR) を手で作り、シード済みの SETTINGS とページを入れる
        jdbc.update("CREATE TABLE SETTINGS (SETTING CHAR(9) NOT NULL, VALUE CHAR(50) NOT NULL DEFAULT '', PRIMARY KEY (SETTING))");
        jdbc.update("INSERT INTO SETTINGS (SETTING, VALUE) VALUES ('ADMPSWRD', 'VCF2024'), ('ALWVOTE', 'Y')");
        jdbc.update("CREATE TABLE LRN400STR (PAGENBR INTEGER NOT NULL, CONTENT CHAR(1500) NOT NULL DEFAULT '',"
                + " EXTRA CHAR(9) NOT NULL DEFAULT '', PRIMARY KEY (PAGENBR))");
        jdbc.update("INSERT INTO LRN400STR (PAGENBR, CONTENT, EXTRA) VALUES (1, 'old common page', 'END')");
        // 他テーブルは schema.sql が作る (SETTINGS/LRN400STR だけ作っておけばよい)
        DataSeeder seeder = new DataSeeder(dataSource, url, "TESTPW");
        seeder.run(new DefaultApplicationArguments());
        seeder.run(new DefaultApplicationArguments());   // 2 回目も冪等

        var repo = new com.vcf400.repository.LearnPageRepository(jdbc);
        assertThat(repo.findFirst("LRN400STR")).get()
            .satisfies(p -> assertThat(p.content()).contains("old common page"));
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME = 'LRN400STR' AND COLUMN_NAME = 'LRNFILE'", Integer.class))
            .isEqualTo(1);
        // (LRNFILE, PAGENBR) 複合キー: 同一 PAGENBR で別ファイルの行を追加できる
        jdbc.update("INSERT INTO LRN400STR (LRNFILE, PAGENBR, CONTENT, EXTRA) VALUES ('ASHIBATA', 1, 'exhibit page', '')");
        assertThat(repo.findByPage("ASHIBATA", 1)).isPresent();
    }

    @Test
    void resumesInterruptedUpgradeWhenLrnfileExistsButPkIsOld(@TempDir Path dir) throws Exception {
        String url = "jdbc:h2:file:" + dir.resolve("vcf400mid")
                + ";MODE=DB2;NON_KEYWORDS=VALUE;DEFAULT_NULL_ORDERING=HIGH";
        DataSource dataSource = dataSource(url);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("CREATE TABLE SETTINGS (SETTING CHAR(9) NOT NULL, VALUE CHAR(50) NOT NULL DEFAULT '', PRIMARY KEY (SETTING))");
        jdbc.update("INSERT INTO SETTINGS (SETTING, VALUE) VALUES ('ADMPSWRD', 'VCF2024'), ('ALWVOTE', 'Y')");
        // 中断した移行の再現: LRNFILE 列は追加済みだが PK が PAGENBR のまま
        jdbc.update("CREATE TABLE LRN400STR (LRNFILE CHAR(10) NOT NULL DEFAULT 'LRN400STR',"
                + " PAGENBR INTEGER NOT NULL, CONTENT CHAR(1500) NOT NULL DEFAULT '',"
                + " EXTRA CHAR(9) NOT NULL DEFAULT '', PRIMARY KEY (PAGENBR))");
        jdbc.update("INSERT INTO LRN400STR (PAGENBR, CONTENT, EXTRA) VALUES (1, 'old common page', 'END')");

        new DataSeeder(dataSource, url, "TESTPW").run(new DefaultApplicationArguments());
        new DataSeeder(dataSource, url, "TESTPW").run(new DefaultApplicationArguments());

        // 別 LRNFILE で同じ PAGENBR を登録できる = 複合 PK
        jdbc.update("INSERT INTO LRN400STR (LRNFILE, PAGENBR, CONTENT, EXTRA) VALUES ('ASHIBATA', 1, 'exhibit page', '')");
        var repo = new com.vcf400.repository.LearnPageRepository(jdbc);
        assertThat(repo.findByPage("ASHIBATA", 1)).isPresent();
        assertThat(repo.findFirst("LRN400STR")).isPresent();
    }

    @Test
    void resumesInterruptedUpgradeWhenPkWasAlreadyDropped(@TempDir Path dir) throws Exception {
        String url = "jdbc:h2:file:" + dir.resolve("vcf400nopk")
                + ";MODE=DB2;NON_KEYWORDS=VALUE;DEFAULT_NULL_ORDERING=HIGH";
        DataSource dataSource = dataSource(url);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("CREATE TABLE SETTINGS (SETTING CHAR(9) NOT NULL, VALUE CHAR(50) NOT NULL DEFAULT '', PRIMARY KEY (SETTING))");
        jdbc.update("INSERT INTO SETTINGS (SETTING, VALUE) VALUES ('ADMPSWRD', 'VCF2024'), ('ALWVOTE', 'Y')");
        // DROP PRIMARY KEY 後・ADD 前に中断した状態: LRNFILE 列あり・PK なし
        jdbc.update("CREATE TABLE LRN400STR (LRNFILE CHAR(10) NOT NULL DEFAULT 'LRN400STR',"
                + " PAGENBR INTEGER NOT NULL, CONTENT CHAR(1500) NOT NULL DEFAULT '',"
                + " EXTRA CHAR(9) NOT NULL DEFAULT '')");
        jdbc.update("INSERT INTO LRN400STR (PAGENBR, CONTENT, EXTRA) VALUES (1, 'old common page', 'END')");

        new DataSeeder(dataSource, url, "TESTPW").run(new DefaultApplicationArguments());
        new DataSeeder(dataSource, url, "TESTPW").run(new DefaultApplicationArguments());

        jdbc.update("INSERT INTO LRN400STR (LRNFILE, PAGENBR, CONTENT, EXTRA) VALUES ('ASHIBATA', 1, 'exhibit page', '')");
        var repo = new com.vcf400.repository.LearnPageRepository(jdbc);
        assertThat(repo.findByPage("ASHIBATA", 1)).isPresent();
        assertThat(repo.findFirst("LRN400STR")).isPresent();
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
