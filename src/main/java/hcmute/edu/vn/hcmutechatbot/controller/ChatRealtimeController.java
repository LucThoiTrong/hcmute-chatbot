package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.ChatRealtimeRequest;
import hcmute.edu.vn.hcmutechatbot.service.ChatRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatRealtimeController {
    private final ChatRealtimeService chatRealtimeService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRealtimeRequest request, Principal principal) {
        if (principal == null) {
            log.error("User unauthenticated!");
            return;
        }

        chatRealtimeService.sendMessage(request, principal);
    }
}
