package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.request.ChatRealtimeRequest;
import hcmute.edu.vn.hcmutechatbot.mapper.MessageMapper;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRealtimeService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public void sendMessage(ChatRealtimeRequest request, Principal principal) {

        log.info("Participants: {}", request.getParticipantIds());

        // 1. Lấy thông tin User hiện tại
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String userId = user.getUsername();

        String conversationId = request.getId();

        // 2. XỬ LÝ CONVERSATION (Tìm hoặc Tạo mới)
        Conversation conversation = conversationRepository.findById(conversationId).orElseGet(() -> createNewConversation(request, userId));

        // 3. Cập nhật thời gian tương tác cuối
        conversation.setLastUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // 4. Xác định SenderType
        SenderType senderType = SenderType.STUDENT;
        String role = user.getAuthorities().stream().findFirst().map(Object::toString).orElse("");
        if (role.contains("LECTURER") || role.contains("FACULTY_HEAD")) {
            senderType = SenderType.LECTURER;
        }

        // 5. Lưu tin nhắn vào DB
        Message message = Message.builder().conversationId(conversationId).content(request.getContent()).senderId(userId).senderType(senderType).sentAt(LocalDateTime.now()).build();

        Message savedMessage = messageRepository.save(message);

        // 6. Gửi tin nhắn ra kênh topic chung
        messagingTemplate.convertAndSend("/topic/chat." + conversationId, messageMapper.toResponse(savedMessage, user.getFullName()));

        log.info("User {} sent message to room {}", userId, conversationId);
    }

    // --- Helper: Logic tạo mới cuộc hội thoại ---
    private Conversation createNewConversation(ChatRealtimeRequest request, String creatorId) {
        log.info("Creating NEW conversation from Realtime Socket: {}", request.getId());

        // Xử lý danh sách người tham gia
        Set<String> participants = request.getParticipantIds();
        if (participants == null) participants = new HashSet<>();
        participants.add(creatorId); // Đảm bảo người tạo có mặt

        // Tạo title mặc định
        String defaultTitle = "Tư vấn hỗ trợ";
        if (request.getType() == ConversationType.ADVISORY && request.getFacultyName() != null) {
            defaultTitle = "Tư vấn - " + request.getFacultyName();
        }

        Conversation newConv = Conversation.builder().id(request.getId()).title(defaultTitle).type(request.getType()).mode(request.getMode()).facultyId(request.getFacultyId()).facultyName(request.getFacultyName()).advisoryDomainId(request.getAdvisoryDomainId()).advisoryDomainName(request.getAdvisoryDomainName()).participantIds(participants).createdByUserId(creatorId).createdAt(LocalDateTime.now()).lastUpdatedAt(LocalDateTime.now()).build();

        Conversation savedConv = conversationRepository.save(newConv);

        // Xử lý logic cho những người tham gia hội thoại update có cuộc hội thoại mới
        for (String participantId : participants) {
            messagingTemplate.convertAndSendToUser(participantId, "/queue/new-conversation", savedConv);
        }

        return savedConv;
    }
}