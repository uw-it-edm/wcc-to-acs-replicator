package edu.uw.edm.wcctoacsreplicator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Maxime Deravet Date: 10/4/18
 */

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = {"edu.uw.edm.wcctoacsreplicator","edu.uw.edm.wcctoacsreplicator.config"})
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.addListeners(new ApplicationPidFileWriter());

        app.run(args);
    }
}
