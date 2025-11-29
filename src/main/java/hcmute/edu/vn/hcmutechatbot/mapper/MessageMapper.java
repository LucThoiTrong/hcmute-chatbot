package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.MessageResponse;
import hcmute.edu.vn.hcmutechatbot.model.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message message, String resolvedName) {
        if (message == null) {
            return null;
        }

        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .sentAt(message.getSentAt())
                .senderName(resolvedName)
                .build();
    }
}