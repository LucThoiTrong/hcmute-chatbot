package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "title", expression = "java(conversation.getTitleForUser(userId))")
    ConversationResponse toResponse(Conversation conversation, String userId);
}