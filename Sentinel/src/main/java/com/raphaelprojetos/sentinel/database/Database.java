package com.raphaelprojetos.sentinel.database;

import com.raphaelprojetos.sentinel.config.DotEnvConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Database {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {

        String endercoAtualBanco = DotEnvConfig.getEnvVar("CENTRAL_IP");
        String usuarioBanco = DotEnvConfig.getEnvVar("DB_USERNAME");
        String nomeBanco = DotEnvConfig.getEnvVar("DB_NOME");
        String senhaBanco = DotEnvConfig.getEnvVar("DB_PASSWORD");

        String jdbc = "jdbc:postgresql://" + endercoAtualBanco + "/" + nomeBanco;

        config.setJdbcUrl(jdbc);
        config.setUsername(usuarioBanco);
        config.setPassword(senhaBanco);
        config.addDataSourceProperty("cachePrepStmts" , "true");
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.setDriverClassName("org.postgresql.Driver");
        ds = new HikariDataSource(config);

    }

    private Database(){}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}

