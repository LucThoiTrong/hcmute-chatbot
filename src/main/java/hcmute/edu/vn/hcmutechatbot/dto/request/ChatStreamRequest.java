package hcmute.edu.vn.hcmutechatbot.dto.request;


import lombok.Data;

@Data
public class ChatStreamRequest {
    private String content;
    private String conversationId;
}