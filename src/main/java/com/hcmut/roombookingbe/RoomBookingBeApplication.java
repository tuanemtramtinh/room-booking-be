package com.hcmut.roombookingbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RoomBookingBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomBookingBeApplication.class, args);
    }

}
