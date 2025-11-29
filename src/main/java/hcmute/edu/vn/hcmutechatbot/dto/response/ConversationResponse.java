package hcmute.edu.vn.hcmutechatbot.dto.response;

import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationStatus;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class ConversationResponse {
    private String id;
    private String title;
    private ConversationType type;
    private ConversationStatus status;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime createdAt;
    private String threadId;
    private Set<String> participantIds;
}