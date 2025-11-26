package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
}
