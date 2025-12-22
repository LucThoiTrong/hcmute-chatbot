package hcmute.edu.vn.hcmutechatbot.config;

import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import hcmute.edu.vn.hcmutechatbot.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Đảm bảo chạy trước các config security mặc định
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    // 1. Cấu hình Interceptor để chặn và check Token
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Chặn mọi message từ client gửi lên server
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                // Chuyển message sang dạng StompHeaderAccessor
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Chỉ kiểm tra khi Client gửi lệnh CONNECT
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Lấy token từ header "Authorization"
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        try {
                            // Validate Token bằng JwtUtils có sẵn của bạn
                            if (jwtUtils.validateJwtToken(token)) {
                                String username = jwtUtils.getUserNameFromJwtToken(token);

                                // Load thông tin User từ DB (hoặc Cache)
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                                // Tạo Authentication object
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                                // Gán User vào phiên WebSocket
                                accessor.setUser(authentication);
                                log.info("WebSocket Authenticated User: {}", username);
                            }
                        } catch (Exception e) {
                            log.error("Lỗi xác thực WebSocket: {}", e.getMessage());
                        }
                    } else {
                        log.warn("WebSocket Connection attempt without Token!");
                    }
                }
                return message;
            }
        });
    }

    // 2. Cấu hình RabbitMQ
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(rabbitHost)
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    // 3. Mở cổng kết nối
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}