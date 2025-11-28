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

    @MessageMapping("/chat.stream")
    public void streamChatMessage(ChatStreamRequest request, Principal principal) {
        // Kiểm tra an toàn
        if (principal == null) {
            log.error("User chưa xác thực (Principal is null)!");
            return;
            // Hoặc bắn tin nhắn lỗi về FE
        }

        // Truyền Principal xuống Service
        chatStreamService.processChatStream(request, principal);
    }
}