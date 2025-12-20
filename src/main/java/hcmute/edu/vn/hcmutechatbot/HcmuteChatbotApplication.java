package hcmute.edu.vn.hcmutechatbot;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class HcmuteChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(HcmuteChatbotApplication.class, args);
    }
    @PostConstruct
    public void init() {
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
}