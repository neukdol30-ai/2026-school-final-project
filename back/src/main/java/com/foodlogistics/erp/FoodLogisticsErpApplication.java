package com.foodlogistics.erp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class FoodLogisticsErpApplication {

    public static void main(String[] args) {

        System.out.println("DB_URL 존재 = " + (System.getenv("DB_URL") != null));
        System.out.println("DB_USERNAME = " + System.getenv("DB_USERNAME"));

        SpringApplication.run(FoodLogisticsErpApplication.class, args);
    }

    @Bean
    CommandLineRunner dbTest(JdbcTemplate jdbcTemplate) {
        return args -> {
            String user = jdbcTemplate.queryForObject(
                    "SELECT USER FROM DUAL",
                    String.class
            );

            System.out.println("============================");
            System.out.println("DB 연결 성공");
            System.out.println("현재 DB USER = " + user);
            System.out.println("============================");
        };
    }
}