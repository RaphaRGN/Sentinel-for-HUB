package com.raphaelprojetos.sentinel.config;

import io.github.cdimascio.dotenv.Dotenv;


public class DotEnvConfig {

   private static Dotenv dotenv = Dotenv.load();

    public static String getEnvVar (String value) {

        if(value == null){

            throw new RuntimeException("Varíavel de ambiente não encontrada pelo valor: " + value);
        }

        return dotenv.get(value);
    }
}
