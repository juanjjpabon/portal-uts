package co.edu.uts.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PortalUtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalUtsApplication.class, args);
    }
}
