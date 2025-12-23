package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.request.ChatRealtimeRequest;
import hcmute.edu.vn.hcmutechatbot.mapper.MessageMapper;
import hcmute.edu.vn.hcmutechatbot.model.Account;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import hcmute.edu.vn.hcmutechatbot.model.Message;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationMode;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import hcmute.edu.vn.hcmutechatbot.model.enums.SenderType;
import hcmute.edu.vn.hcmutechatbot.repository.AccountRepository;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.MessageRepository;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRealtimeService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LecturerRepository lecturerRepository;
    private final AccountRepository accountRepository;
    private final MessageMapper messageMapper;

    public void sendMessage(ChatRealtimeRequest request, Principal principal) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String userId = user.getUsername();
        String conversationId = request.getId();

        // 1. Tìm hoặc tạo mới hội thoại
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseGet(() -> createNewConversation(request, userId));

        conversation.setLastUpdatedAt(LocalDateTime.now());

        // 2. Nếu là cuộc hội thoại public -> kéo toàn bộ giảng viên vào cuộc hội thoại
        if (conversation.getMode() == ConversationMode.PUBLIC) {
            ensureAllLecturersInConversation(conversation);
        }

        Conversation updatedConversation = conversationRepository.save(conversation);

        // 3. Kiểm tra người gửi là sinh viên hay giảng viên và Lưu message
        SenderType senderType = SenderType.STUDENT;
        String roles = user.getAuthorities().toString();
        if (roles.contains("LECTURER") || roles.contains("FACULTY_HEAD")) {
            senderType = SenderType.LECTURER;
        }

        // 4. Lưu message xuống db
        Message message = Message.builder()
                .conversationId(conversationId)
                .content(request.getContent())
                .senderId(userId)
                .senderType(senderType)
                .sentAt(LocalDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        // 5. Gửi message đến kênh chung.
        messagingTemplate.convertAndSend("/topic/chat." + conversationId,
                messageMapper.toResponse(savedMessage, user.getFullName()));

        // 6. Gửi tín hiện cho từng người tham gia đoạn chat -> đẩy cuộc hội thoại đó lên đầu.
        Set<String> participantIds = updatedConversation.getParticipantIds();
        if (participantIds != null) {
            for (String participantId : participantIds) {
                if (!participantId.equals(userId)) {
                    messagingTemplate.convertAndSendToUser(
                            participantId,
                            "/queue/conversation-updates",
                            updatedConversation
                    );
                }
            }
        }
    }

    // --- Helper 1: Tạo mới hội thoại ---
    private Conversation createNewConversation(ChatRealtimeRequest request, String creatorId) {
        log.info("Creating NEW conversation: {}", request.getId());

        Set<String> participants = request.getParticipantIds();
        if (participants == null) participants = new HashSet<>();
        participants.add(creatorId);

        // Thêm toàn bộ Username của giảng viên vào danh sách tham gia cuộc hội thoại public
        if (request.getMode() == ConversationMode.PUBLIC && request.getFacultyId() != null) {
            List<String> lecturerUsernames = getLecturerUsernamesByFaculty(request.getFacultyId());
            if (!lecturerUsernames.isEmpty()) {
                participants.addAll(lecturerUsernames);
                log.info("Added {} lecturers (usernames) to Public Chat", lecturerUsernames.size());
            }
        }

        String defaultTitle = "Tư vấn hỗ trợ";
        if (request.getType() == ConversationType.ADVISORY && request.getFacultyName() != null) {
            defaultTitle = "Tư vấn - " + request.getFacultyName();
        }

        Conversation newConv = Conversation.builder()
                .id(request.getId())
                .title(defaultTitle)
                .type(request.getType())
                .mode(request.getMode())
                .facultyId(request.getFacultyId())
                .facultyName(request.getFacultyName())
                .advisoryDomainId(request.getAdvisoryDomainId())
                .advisoryDomainName(request.getAdvisoryDomainName())
                .participantIds(participants)
                .createdByUserId(creatorId)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Conversation savedConv = conversationRepository.save(newConv);

        // Gửi thông báo cho các người dùng vừa được mời tham gia 1 hội thoại mới.
        for (String participantId : participants) {
            if (!participantId.equals(creatorId)) {
                messagingTemplate.convertAndSendToUser(participantId, "/queue/new-conversation", savedConv);
            }
        }
        return savedConv;
    }

    // --- Helper 2: Đồng bộ danh sách giảng viên ---
    private void ensureAllLecturersInConversation(Conversation conversation) {
        if (conversation.getFacultyId() == null) return;

        // Gọi hàm helper tách biệt để lấy List Username
        List<String> allLecturerUsernames = getLecturerUsernamesByFaculty(conversation.getFacultyId());

        Set<String> currentParticipants = conversation.getParticipantIds();
        if (currentParticipants == null) currentParticipants = new HashSet<>();

        boolean changed = false;
        for (String username : allLecturerUsernames) {
            if (!currentParticipants.contains(username)) {
                currentParticipants.add(username);
                changed = true;
            }
        }

        if (changed) {
            conversation.setParticipantIds(currentParticipants);
        }
    }

    // --- Helper 3: Hàm cốt lõi để map từ Faculty -> Lecturer -> Account -> Username ---
    private List<String> getLecturerUsernamesByFaculty(String facultyId) {
        try {
            // Bước 1: Lấy tất cả Lecturer thuộc khoa
            List<Lecturer> lecturers = lecturerRepository.findByFacultyId(facultyId);
            if (lecturers.isEmpty()) {
                return Collections.emptyList();
            }

            // Bước 2: Lấy danh sách ID của Lecturer
            List<String> lecturerIds = lecturers.stream()
                    .map(Lecturer::getId)
                    .collect(Collectors.toList());

            // Bước 3: Tìm Account dựa trên ownerId
            List<Account> accounts = accountRepository.findByOwnerIdIn(lecturerIds);

            // Bước 4: Lấy Username từ Account
            return accounts.stream()
                    .map(Account::getUsername)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching lecturer usernames for faculty: {}", facultyId, e);
            return Collections.emptyList();
        }
    }
}