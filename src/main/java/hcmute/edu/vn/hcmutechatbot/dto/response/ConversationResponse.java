package hcmute.edu.vn.hcmutechatbot.dto.response;

import hcmute.edu.vn.hcmutechatbot.model.Conversation;
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

    private Set<String> participantIds;

    // Helper method để map từ Entity sang DTO
    public static ConversationResponse from(Conversation conversation, String userId) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitleForUser(userId))
                .type(conversation.getType())
                .status(conversation.getStatus())
                .lastUpdatedAt(conversation.getLastUpdatedAt())
                .createdAt(conversation.getCreatedAt())
                .participantIds(conversation.getParticipantIds())
                .build();
    }
}