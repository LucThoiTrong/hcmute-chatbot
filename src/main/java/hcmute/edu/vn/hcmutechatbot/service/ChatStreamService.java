package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.request.ChatStreamRequest;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.Message;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import hcmute.edu.vn.hcmutechatbot.model.enums.SenderType;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import hcmute.edu.vn.hcmutechatbot.repository.MessageRepository;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatStreamService {

    private final WebClient aiWebClient;
    private final SimpMessagingTemplate messagingTemplate;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public void processChatStream(ChatStreamRequest request, Principal principal) {

        // 1. Lấy thông tin User
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String userId = user.getUsername();

        String conversationId = request.getConversationId();


        // 2. Xử lý Conversation (Tạo mới hoặc Lấy cũ)
        boolean isNewConversation = false;
        Conversation conversation;

        // Tìm conversation bằng khóa chính (ID)
        Optional<Conversation> existingConv = conversationRepository.findById(conversationId);

        if (existingConv.isPresent()) {
            conversation = existingConv.get();
            // Cập nhật thời gian tương tác cuối
            conversation.setLastUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conversation);

            log.debug("Found existing conversation: {}", conversationId);
        } else {
            // TẠO MỚI CONVERSATION
            isNewConversation = true;
            log.info("Creating NEW LOOKUP conversation with ID: {}", conversationId);

            // Tạo tiêu đề tự động từ tin nhắn đầu tiên (lấy 50 ký tự đầu)
            String autoTitle = request.getContent().length() > 50
                    ? request.getContent().substring(0, 50) + "..."
                    : request.getContent();

            conversation = Conversation.builder()
                    .id(conversationId)
                    .threadId(conversationId)
                    .title(autoTitle)
                    .createdByUserId(userId)
                    .createdAt(LocalDateTime.now())
                    .lastUpdatedAt(LocalDateTime.now())
                    .type(ConversationType.LOOKUP)
                    .participantIds(Set.of(userId))
                    .build();

            conversationRepository.save(conversation);
        }

        // 3. LƯU TIN NHẮN CỦA USER VÀO DB
        Message userMsg = Message.builder()
                .conversationId(conversation.getId())
                .content(request.getContent())
                .senderId(userId)
                .senderType(SenderType.STUDENT) // Giả định người dùng là STUDENT
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(userMsg);

        // 4. BẮN TÍN HIỆU UPDATE SIDEBAR (Chỉ khi là hội thoại mới)
        if (isNewConversation) {
            // Gửi một event riêng để FE biết mà thêm vào list conversation
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/new-conversation",
                    conversation // Gửi Conversation Object mới
            );
        }

        // 5. Chuẩn bị gọi AI
        String role = user.getAuthorities().stream().findFirst().map(Object::toString).orElse("GUEST");
        Map<String, Object> userInfo = Map.of(
                "full_name", user.getFullName(),
                "role", role,
                "user_id", userId
        );

        Map<String, Object> aiRequest = Map.of(
                "input", request.getContent(),
                "thread_id", conversationId, // Dùng threadId (chính là conversationId)
                "user_info", userInfo
        );

        // StringBuilder để gom toàn bộ text stream từ AI
        StringBuilder fullAiResponse = new StringBuilder();
        Conversation finalConversation = conversation; // Biến final để dùng trong lambda

        // 6. Gọi AI & Xử lý Stream
        aiWebClient.post()
                .uri("/chat")
                .bodyValue(aiRequest)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        token -> {
                            // A. Gửi từng chunk về cho FE hiển thị (Stream)
                            messagingTemplate.convertAndSendToUser(
                                    userId,
                                    "/queue/stream-reply",
                                    token
                            );
                            fullAiResponse.append(token);
                        },
                        error -> {
                            log.error("Lỗi stream AI cho ID {}: ", conversationId, error);
                            messagingTemplate.convertAndSendToUser(userId, "/queue/stream-reply", "[ERR_AI]");
                        },
                        () -> {
                            // C. KHI STREAM HOÀN TẤT (ON COMPLETE)
                            log.debug("Hoàn tất stream cho ID: {}", conversationId);

                            // LƯU TIN NHẮN CỦA AI VÀO DB
                            Message aiMsg = Message.builder()
                                    .conversationId(finalConversation.getId())
                                    .content(fullAiResponse.toString())
                                    .senderId("AI_AGENT")
                                    .senderType(SenderType.BOT)
                                    .sentAt(LocalDateTime.now())
                                    .build();
                            messageRepository.save(aiMsg);
                        }
                );
    }
}