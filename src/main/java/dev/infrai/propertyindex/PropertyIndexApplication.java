package dev.infrai.propertyindex;

import dev.infrai.propertyindex.config.InfraiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InfraiProperties.class)
public class PropertyIndexApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropertyIndexApplication.class, args);
    }
}
