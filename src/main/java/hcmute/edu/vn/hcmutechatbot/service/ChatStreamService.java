package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.request.ChatStreamRequest;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatStreamService {

    private final WebClient aiWebClient;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Xử lý luồng chat: Nhận Principal -> Convert User -> Gọi AI
     */
    public void processChatStream(ChatStreamRequest request, Principal principal) {

        // 1. Convert Principal về CustomUserDetails
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        // 2. Tạo User Info map (Logic này chuyển từ Interface vào đây cho an toàn)
        String role = user.getAuthorities().stream().findFirst().map(Object::toString).orElse("GUEST");

        Map<String, Object> userInfo = Map.of(
                "full_name", user.getFullName(),
                "role", role,
                "user_id", user.getUsername()
        );

        log.info("User [{}] đang chat: {}", userInfo.get("user_id"), request.getContent());

        // 3. Xử lý ThreadID
        String rawThreadId = request.getThreadId();

        // 2. Tính toán giá trị cuối cùng (nếu null thì tạo mới)
        if (rawThreadId == null || rawThreadId.trim().isEmpty()) {
            rawThreadId = "session_" + UUID.randomUUID();
        }

        String finalThreadId = rawThreadId;

        // 4. Payload gửi sang AI
        Map<String, Object> aiRequest = Map.of(
                "input", request.getContent(),
                "thread_id", finalThreadId,
                "user_info", userInfo
        );

        // 5. Gọi AI Agent
        aiWebClient.post()
                .uri("/chat")
                .bodyValue(aiRequest)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        token -> messagingTemplate.convertAndSendToUser(
                                user.getUsername(), // Gửi đích danh cho User ID này
                                "/queue/stream-reply", // Đuôi của kênh nhận
                                token
                        ),
                        error -> {
                            log.error("Lỗi stream AI: ", error);
                            messagingTemplate.convertAndSend("/topic/stream-reply", "[ERR_AI]");
                        },
                        () -> log.debug("Hoàn tất stream: {}", finalThreadId)
                );
    }
}