package com.pm.pulseserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({com.pm.pulseserver.modules.auth.infra.AppAuthProperties.class})
@SpringBootApplication
public class PulseServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PulseServerApplication.class, args);
    }

}
