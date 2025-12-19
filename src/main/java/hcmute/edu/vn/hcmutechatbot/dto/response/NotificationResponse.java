package hcmute.edu.vn.hcmutechatbot.dto.response;

import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private String title;
    private String content;
    private String senderId;
    private String senderName;
    private NotificationScope scope;
    private String targetId;
    private LocalDateTime timestamp;
    private boolean isRead;
}