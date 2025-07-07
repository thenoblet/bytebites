package gtp.bytebites.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "gtp.bytebites")
@EnableDiscoveryClient
public class NotificationService {
    public static void main(String[] args) {
        SpringApplication.run(NotificationService.class, args);
    }
}
