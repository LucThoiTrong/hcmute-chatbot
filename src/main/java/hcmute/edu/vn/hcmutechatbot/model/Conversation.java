package hcmute.edu.vn.hcmutechatbot.model;

import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationMode;
//import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;

    private String title;

    @Builder.Default
    private Map<String, String> userTitles = new HashMap<>();

    @Builder.Default
    private Set<String> deletedByUserIds = new HashSet<>();

    private ConversationType type; // LOOKUP, ADVISORY
    private ConversationMode mode; // PRIVATE, PUBLIC

    private LocalDateTime createdAt;
//    private ConversationStatus status; // PENDING, OPEN, CLOSED
    private LocalDateTime lastUpdatedAt;

    private String createdByUserId;
    private String facultyId;
    private String facultyName;

    private String advisoryDomainId;
    private String advisoryDomainName;

    private String thread_id;
    private Set<String> participantIds; // Lưu trữ IDs của người tham gia (human users)
    private Map<String, String> participantStates;

    // Get Title For Conversation
    public String getTitleForUser(String userId) {
        if (userTitles != null && userTitles.containsKey(userId)) {
            return userTitles.get(userId);
        }
        return this.title;
    }

}