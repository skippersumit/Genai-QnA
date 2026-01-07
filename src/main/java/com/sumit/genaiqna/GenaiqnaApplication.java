package com.sumit.genaiqna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// TODO i've added this exclude line, remove if not required.
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GenaiqnaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenaiqnaApplication.class, args);
    }

}
