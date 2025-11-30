package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.UpdateConversationTitleRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.MessageResponse;
import hcmute.edu.vn.hcmutechatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/history")
    public ResponseEntity<Page<ConversationResponse>> getUserChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(chatService.getConversationsByUserId(page, size, keyword));
    }

    @PatchMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> updateConversationTitle(
            @PathVariable String conversationId,
            @RequestBody UpdateConversationTitleRequest request
    ) {
        String newTitle = request.getTitle();

        if (newTitle == null || newTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(chatService.updateConversationTitle(conversationId, newTitle));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        chatService.softDeleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(chatService.getConversationMessages(conversationId, page, size));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversationInfo(
            @PathVariable String conversationId
    ) {
        return ResponseEntity.ok(chatService.getConversationById(conversationId));
    }
}