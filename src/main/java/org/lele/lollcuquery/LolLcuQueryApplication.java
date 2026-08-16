package org.lele.lollcuquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LolLcuQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LolLcuQueryApplication.class, args);
    }

}
