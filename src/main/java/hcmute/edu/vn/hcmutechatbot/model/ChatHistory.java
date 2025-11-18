package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {
    @Id
    private String id;
    private String title;
    private Set<Message> messages;
    private LocalDateTime createdAt;
    private String studentId;
}
