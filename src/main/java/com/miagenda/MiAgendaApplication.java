package com.miagenda;

import com.miagenda.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MiAgendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiAgendaApplication.class, args);
    }
}
