package com.vcf400.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/** H2 の空 DB にだけクロス検証用の初期データを投入する。 */
@Component
public class DataSeeder implements ApplicationRunner {
    private final DataSource dataSource;
    private final String jdbcUrl;

    public DataSeeder(DataSource dataSource, @Value("${spring.datasource.url}") String jdbcUrl) {
        this.dataSource = dataSource;
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:h2:")) {
            return;
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM SETTINGS");
             ResultSet result = statement.executeQuery()) {
            if (!result.next() || result.getInt(1) != 0) {
                return;
            }
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("seed.sql"));
        }
    }
}
