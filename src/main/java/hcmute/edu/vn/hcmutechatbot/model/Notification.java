package hcmute.edu.vn.hcmutechatbot.model;

import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;

    private String title;
    private String content;
    private String senderId;
    private NotificationScope scope;
    private String targetId;
    private LocalDateTime timestamp;
    @Builder.Default
    private Set<String> readByUserIds = new HashSet<>();

    public boolean checkIsRead(Notification notification, String userId) {
        if (notification.getReadByUserIds() == null) {
            return false;
        }
        return notification.getReadByUserIds().contains(userId);
    }
}