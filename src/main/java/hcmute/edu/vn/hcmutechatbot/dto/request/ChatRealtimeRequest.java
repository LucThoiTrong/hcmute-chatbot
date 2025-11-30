package hcmute.edu.vn.hcmutechatbot.dto.request;

import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationMode;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRealtimeRequest {

    // 1. ID từ Frontend gửi (UUID)
    private String id;

    // 2. Nội dung tin nhắn
    private String content;

    // 3. Metadata để tạo hội thoại
    private ConversationType type;
    private ConversationMode mode;
    private Set<String> participantIds;

    // Context tư vấn
    private String facultyId;
    private String facultyName;
    private String advisoryDomainId;
    private String advisoryDomainName;
}