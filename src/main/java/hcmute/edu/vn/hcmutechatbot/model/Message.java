package hcmute.edu.vn.hcmutechatbot.model;

import hcmute.edu.vn.hcmutechatbot.model.enums.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "messages")
public class Message {
    @Id
    private String id;

    private String conversationId;
    private String content;

    private String senderId;
    private SenderType senderType; // USER, ADVISOR, BOT
    private LocalDateTime sentAt;
}