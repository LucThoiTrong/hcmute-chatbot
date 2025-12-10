package hcmute.edu.vn.hcmutechatbot.dto.request;

import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import lombok.Data;

@Data
public class CreateNotificationRequest {
    private String title;
    private String content;
    private NotificationScope scope; // INDIVIDUAL, FACULTY_ALL, FACULTY_STUDENT...
    private String targetId;         // ID người nhận (nếu scope là INDIVIDUAL)
}