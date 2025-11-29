package hcmute.edu.vn.hcmutechatbot.dto.response;

import hcmute.edu.vn.hcmutechatbot.model.enums.SenderType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String content;
    private String senderId;
    private String senderName;
    private SenderType senderType;
    LocalDateTime sentAt;
}