package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    // 1. Hàm dùng cho List History
    @Mapping(target = "isUnread", source = "isUnread")
    @Mapping(target = "title", expression = "java(conversation.getTitleForUser(userId))")
    ConversationResponse toResponse(Conversation conversation, String userId, boolean isUnread);

    // 2. Khi lấy chi tiết để xem, mặc định coi như là "Đã đọc" (isUnread = false)
    @Mapping(target = "title", expression = "java(conversation.getTitleForUser(userId))")
    @Mapping(target = "isUnread", constant = "false") // Set cứng false
    ConversationResponse toDetailResponse(Conversation conversation, String userId);
}