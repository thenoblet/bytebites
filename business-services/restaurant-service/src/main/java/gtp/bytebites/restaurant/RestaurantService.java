package gtp.bytebites.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "gtp")
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class RestaurantService {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantService.class, args);
    }
}
