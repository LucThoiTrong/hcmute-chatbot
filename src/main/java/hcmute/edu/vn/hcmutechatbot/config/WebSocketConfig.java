package hcmute.edu.vn.hcmutechatbot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Cấu hình để springboot lấy tin nhắn ở cổng 61613
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)            // Cổng STOMP của RabbitMQ
                .setClientLogin("guest")        // User mặc định
                .setClientPasscode("guest");    // Pass mặc định

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Mở cổng kết nối WebSocket
        registry.addEndpoint("/ws")
                // Cho phép React (port 5173) kết nối vào
                .setAllowedOrigins("http://localhost:5173", "*")
                .withSockJS(); // Kích hoạt SockJS fallback
    }
}
