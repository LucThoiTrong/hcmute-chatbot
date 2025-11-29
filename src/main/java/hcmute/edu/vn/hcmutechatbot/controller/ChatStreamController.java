package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.ChatStreamRequest;
import hcmute.edu.vn.hcmutechatbot.service.ChatStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStreamController {

    private final ChatStreamService chatStreamService;

    // Principal là user đã được xác thực khi connect
    @MessageMapping("/chat.stream")
    public void streamChatMessage(ChatStreamRequest request, Principal principal) {
        // Kiểm tra an toàn
        if (principal == null) {
            log.error("User chưa xác thực (Principal is null)!");
            return;
        }

        // Truyền Principal xuống Service
        chatStreamService.processChatStream(request, principal);
    }
}