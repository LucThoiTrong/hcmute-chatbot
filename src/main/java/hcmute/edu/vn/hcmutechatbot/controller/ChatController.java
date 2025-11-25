package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.UpdateConversationTitleRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import hcmute.edu.vn.hcmutechatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ConversationResponse>> getUserChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(chatService.getConversationsByUserId(userId, page, size));
    }

    @PatchMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> updateConversationTitle(
            @PathVariable String conversationId,
            @RequestBody UpdateConversationTitleRequest request
    ) {
        String userId = getCurrentUserId();
        String newTitle = request.getTitle();

        if (newTitle == null || newTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(chatService.updateConversationTitle(conversationId, userId, newTitle));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        String userId = getCurrentUserId();
        chatService.softDeleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }
}