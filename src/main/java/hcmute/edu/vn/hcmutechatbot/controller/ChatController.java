package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.UpdateConversationTitleRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/history")
    public ResponseEntity<Page<ConversationResponse>> getUserChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(chatService.getConversationsByUserId(page, size));
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
}