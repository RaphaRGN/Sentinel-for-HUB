package com.raphaelprojetos.sentinel.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Database {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;


    static {

        String endercoAtualBanco = System.getenv("DB_HOST");
        String usuarioBanco = System.getenv("DB_USER");
        String nomeBanco = "Sentinel";
        String senhaBanco = System.getenv("DB_PASSWORD");

        String jdbc = "jdbc:postgresql://" + endercoAtualBanco + "/" + nomeBanco;

        System.out.println(jdbc);
        System.out.println(usuarioBanco);
        System.out.println(senhaBanco);

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

