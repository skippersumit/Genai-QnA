package com.sumit.genaiqna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

// TODO i've added this exclude line, remove if not required.
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableCaching
public class GenaiqnaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenaiqnaApplication.class, args);
    }

}
