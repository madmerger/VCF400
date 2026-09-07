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

/** H2 の空 DB にだけクロス検証用の初期データを投入する。 */
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
                    if (!settingsEmpty(connection)) {
                        connection.commit();
                        return;
                    }
                    ScriptUtils.executeSqlScript(connection, new ClassPathResource("seed.sql"));
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE SETTINGS SET VALUE=? WHERE SETTING='ADMPSWRD'")) {
                        statement.setString(1, adminPassword);
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
