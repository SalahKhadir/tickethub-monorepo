package com.tickethub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public final class TickethubApplication {

    private TickethubApplication() {
        // Utility class
    }

    /**
     * Javadoc.
      * @param args description
     */
    public static void main(final String[] args) {
        SpringApplication.run(TickethubApplication.class, args);
    }

}
