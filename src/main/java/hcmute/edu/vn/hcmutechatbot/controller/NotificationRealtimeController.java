package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.CreateNotificationRequest;
import hcmute.edu.vn.hcmutechatbot.service.NotificationRealtimeService;
import hcmute.edu.vn.hcmutechatbot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationRealtimeController {

    private final NotificationRealtimeService notificationRealtimeService;

    @MessageMapping("/notification.create")
    public void createNotification(@Payload CreateNotificationRequest request, Principal principal) {
        if (principal == null) {
            log.error("User unauthenticated!");
            return;
        }

        log.info("Nhận yêu cầu tạo thông báo qua WebSocket từ: {}", principal.getName());

        // Gọi service xử lý (Lưu DB -> Bắn socket tới người nhận)
        notificationRealtimeService.createAndSendNotification(request, principal);
    }
}