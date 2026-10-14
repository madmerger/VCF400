package com.vcf400.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptStatementFailedException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** H2 では schema.sql を適用し、空 DB にだけクロス検証用の初期データを投入する。 */
@Component
public class DataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DataSource dataSource;
    private final String jdbcUrl;
    private final String adminPassword;

    public DataSeeder(DataSource dataSource, @Value("${spring.datasource.url}") String jdbcUrl,
                      @Value("${vcf.seed.admin-password}") String adminPassword) {
        this.dataSource = dataSource;
        this.jdbcUrl = jdbcUrl;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:h2:")) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                try {
                    ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
                    upgradeLrn400str(connection);
                    if (!settingsEmpty(connection)) {
                        connection.commit();
                        return;
                    }
                    ScriptUtils.executeSqlScript(connection, new ClassPathResource("seed.sql"));
                    if (adminPassword == null || adminPassword.isEmpty()) {
                        log.warn("VCF_SEED_ADMPSWRD 未設定のためキオスク終了は無効");
                    }
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE SETTINGS SET VALUE=? WHERE SETTING='ADMPSWRD'")) {
                        statement.setString(1, adminPassword == null ? "" : adminPassword);
                        statement.executeUpdate();
                    }
                    connection.commit();
                } catch (Exception e) {
                    rollback(connection, e);
                    if (isIntegrityViolation(e)) {
                        if (!settingsEmpty(connection)) {
                            log.info("seed skipped: already seeded by another instance");
                            return;
                        }
                        throw new IllegalStateException(
                                "seed failed: database is partially initialized (SETTINGS empty)", e);
                    }
                    throw e;
                }
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * 旧スキーマ (PK PAGENBR のみ、LRNFILE 列なし) で作成済みの LRN400STR を
     * (LRNFILE, PAGENBR) PK へアップグレードする。既存行は LRNFILE='LRN400STR' (既定値) で保持される。
     */
    private static void upgradeLrn400str(Connection connection) throws Exception {
        // H2 の DDL は自動コミットされるため中断に備えて各ステップを独立に冪等化する
        if (!hasLrnfileColumn(connection)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "ALTER TABLE LRN400STR ADD COLUMN LRNFILE CHAR(10) NOT NULL DEFAULT 'LRN400STR' BEFORE PAGENBR")) {
                statement.executeUpdate();
            }
        }
        java.util.List<String> pkColumns = primaryKeyColumns(connection);
        if (pkColumns.equals(java.util.List.of("LRNFILE", "PAGENBR"))) {
            return;
        }
        if (!pkColumns.isEmpty()) {  // DROP 後・ADD 前に中断した場合は PK なし → スキップ
            try (PreparedStatement statement = connection.prepareStatement(
                    "ALTER TABLE LRN400STR DROP PRIMARY KEY")) {
                statement.executeUpdate();
            } catch (Exception dropFailure) {
                String pkName = primaryKeyName(connection);
                if (pkName == null) {
                    throw dropFailure;
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "ALTER TABLE LRN400STR DROP CONSTRAINT " + pkName)) {
                    statement.executeUpdate();
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "ALTER TABLE LRN400STR ADD PRIMARY KEY (LRNFILE, PAGENBR)")) {
            statement.executeUpdate();
        }
        log.info("LRN400STR を LRNFILE 付きスキーマへアップグレードしました");
    }

    private static boolean hasLrnfileColumn(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME = 'LRN400STR' AND COLUMN_NAME = 'LRNFILE'");
             ResultSet result = statement.executeQuery()) {
            return result.next() && result.getInt(1) > 0;
        }
    }

    /** 現在の PK 列 (順序付き)。PK なしなら空リスト。 */
    private static java.util.List<String> primaryKeyColumns(Connection connection) throws Exception {
        try (ResultSet keys = connection.getMetaData().getPrimaryKeys(null, null, "LRN400STR")) {
            java.util.List<String> columns = new java.util.ArrayList<>();
            while (keys.next()) {
                columns.add(keys.getString("COLUMN_NAME"));
            }
            return columns;
        }
    }

    private static String primaryKeyName(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                        + "WHERE TABLE_NAME = 'LRN400STR' AND CONSTRAINT_TYPE = 'PRIMARY KEY'");
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getString(1) : null;
        }
    }

    private static boolean settingsEmpty(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM SETTINGS");
             ResultSet result = statement.executeQuery()) {
            return result.next() && result.getInt(1) == 0;
        }
    }

    private static void rollback(Connection connection, Exception failure) {
        try {
            connection.rollback();
        } catch (Exception rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }

    private static boolean isIntegrityViolation(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
        }
        return failure instanceof ScriptStatementFailedException
                && isIntegrityViolation(failure.getCause());
    }
}
