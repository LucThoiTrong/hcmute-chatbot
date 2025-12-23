package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.MessageResponse;
import hcmute.edu.vn.hcmutechatbot.exception.ResourceNotFoundException;
import hcmute.edu.vn.hcmutechatbot.exception.AccessDeniedException;
import hcmute.edu.vn.hcmutechatbot.mapper.ConversationMapper;
import hcmute.edu.vn.hcmutechatbot.mapper.MessageMapper;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import hcmute.edu.vn.hcmutechatbot.model.Message;
import hcmute.edu.vn.hcmutechatbot.model.Student;
import hcmute.edu.vn.hcmutechatbot.model.enums.SenderType;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.MessageRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
import hcmute.edu.vn.hcmutechatbot.security.ISecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService implements ISecurityService {
    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    // 1. Lấy danh sách chat
    public Page<ConversationResponse> getConversationsByUserId(int page, int size, String keyword) {
        String userId = getCurrentUserId();

        // Sort theo thời gian update mới nhất
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdatedAt").descending());

        Page<Conversation> conversationPage;

        // Kiểm tra nếu keyword có dữ liệu hay không
        if (keyword != null && !keyword.trim().isEmpty()) {
            // CASE 1: Có keyword -> Search
            conversationPage = conversationRepository.findByParticipantIdsContainsAndDeletedByUserIdsNotContainsAndTitleContainingIgnoreCase(
                    userId, // participantId
                    userId, // deletedUserId
                    keyword.trim(), // Xóa khoảng trắng thừa
                    pageable
            );
        } else {
            conversationPage = conversationRepository.findByParticipantIdsContainsAndDeletedByUserIdsNotContains(
                    userId,
                    userId,
                    pageable
            );
        }

        return conversationPage.map(conversation -> {
            boolean isUnread = false;

            // 1. Query lấy tin nhắn mới nhất của cuộc hội thoại này
            Message latestMessage = messageRepository.findFirstByConversationIdOrderBySentAtDesc(conversation.getId()).orElse(null);

            if (latestMessage != null) {
                // Lấy ID tin nhắn mà user đã đọc lần cuối.
                String lastReadMsgId = "";
                if (conversation.getParticipantStates() != null) {
                    lastReadMsgId = conversation.getParticipantStates().getOrDefault(userId, "");
                }

                String latestMsgId = latestMessage.getId();
                // 1. ID tin mới nhất KHÁC tin đã đọc
                // 2. VÀ Người gửi tin mới nhất KHÔNG PHẢI là chính mình (Mình gửi thì ko tính là unread)
                if (!latestMsgId.equals(lastReadMsgId) && !latestMessage.getSenderId().equals(userId)) {
                    isUnread = true;
                }
            }
            return conversationMapper.toResponse(conversation, userId, isUnread);
        });
    }

    // 2. Hàm Patch cập nhật tiêu đề cuộc hội thoại (userTitles)
    public ConversationResponse updateConversationTitle(String conversationId, String newTitle) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        String userId = getCurrentUserId();

        // Kiểm tra xem user có trong cuộc hội thoại không
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new AccessDeniedException("User is not a participant of this conversation");
        }

        // Cập nhật title riêng cho user này
        conversation.getUserTitles().put(userId, newTitle);

        Conversation updatedConversation = conversationRepository.save(conversation);

        return conversationMapper.toDetailResponse(updatedConversation, userId);
    }

    // 3. Hàm Patch cập nhật xóa mềm (deletedByUserIds)
    public void softDeleteConversation(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        String userId = getCurrentUserId();

        if (conversation.getDeletedByUserIds() == null) {
            conversation.setDeletedByUserIds(new HashSet<>());
        }

        // Thêm userId vào set deleted (Set sẽ tự loại bỏ trùng lặp)
        conversation.getDeletedByUserIds().add(userId);

        conversationRepository.save(conversation);
    }

    public Page<MessageResponse> getConversationMessages(String conversationId, int page, int size) {
        String userId = getCurrentUserId();

        // 1. Kiểm tra Conversation tồn tại
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // 2. Kiểm tra quyền truy cập (User phải có trong participantIds)
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập cuộc hội thoại này");
        }

        // 3. Query DB lấy tin nhắn (Sắp xếp MỚI NHẤT lên đầu để lazy load đúng chuẩn)
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<Message> messagePage = messageRepository.findByConversationId(conversationId, pageable);

        // CẬP NHẬT TRẠNG THÁI ĐÃ ĐỌC (MARK AS READ)
        // Mục đích: Để khi user quay ra danh sách chat, chấm xanh (unread) sẽ biến mất.
        // Điều kiện: Chỉ update khi user đang xem trang đầu tiên (tin mới nhất) và có tin nhắn.
        if (page == 0 && messagePage.hasContent()) {
            Message latestMsg = messagePage.getContent().getFirst();
            String latestMsgId = latestMsg.getId();

            // Khởi tạo map nếu chưa có (Null Safety)
            if (conversation.getParticipantStates() == null) {
                conversation.setParticipantStates(new HashMap<>());
            }
            Map<String, String> states = conversation.getParticipantStates();

            // Lấy ID tin nhắn đã đọc hiện tại trong DB
            String currentReadId = states.getOrDefault(userId, "");

            if (!latestMsgId.equals(currentReadId)) {
                states.put(userId, latestMsgId);
                conversationRepository.save(conversation);
            }
        }
        // =========================================================================

        // --- BẮT ĐẦU LOGIC TRA CỨU TÊN ĐA BẢNG (OPTIMIZED) ---

        // 4. Tách ID ra 2 danh sách riêng biệt: StudentIDs và LecturerIDs
        Set<String> studentIds = new HashSet<>();
        Set<String> lecturerIds = new HashSet<>();

        for (Message msg : messagePage.getContent()) {
            if (msg.getSenderType() == SenderType.STUDENT) {
                studentIds.add(msg.getSenderId());
            } else if (msg.getSenderType() == SenderType.LECTURER) {
                lecturerIds.add(msg.getSenderId());
            }
        }

        // 5. Query Bulk (Chỉ query nếu danh sách ID không rỗng)
        // Map dùng để lưu kết quả: <ID, FullName>
        Map<String, String> namesMap = new HashMap<>();

        // A. Tra bảng Sinh viên (Select * from students where id IN (...))
        if (!studentIds.isEmpty()) {
            List<Student> students = studentRepository.findAllById(studentIds);
            students.forEach(s -> namesMap.put(s.getStudentId(), s.getFullName()));
        }

        // B. Tra bảng Giảng viên (Select * from lecturers where id IN (...))
        if (!lecturerIds.isEmpty()) {
            List<Lecturer> lecturers = lecturerRepository.findAllById(lecturerIds);
            lecturers.forEach(l -> namesMap.put(l.getId(), l.getFullName()));
        }

        // 6. Map vào DTO trả về (Kết hợp Message + Tên vừa tra được)
        return messagePage.map(msg -> {
            String displayName;

            if (msg.getSenderType() == SenderType.BOT) {
                displayName = null;
            } else {
                displayName = namesMap.getOrDefault(msg.getSenderId(), "Người dùng");
            }

            // Gọi Mapper để build response
            return messageMapper.toResponse(msg, displayName);
        });
    }

    public ConversationResponse getConversationById(String conversationId) {
        String userId = getCurrentUserId();

        // 1. Tìm trong DB
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // 2. Security Check: Người dùng có nằm trong cuộc hội thoại này không?
        // Nếu không có dòng này, ai biết ID cũng xem được trộm tin nhắn -> Lỗi bảo mật to
        if (conversation.getParticipantIds() == null || !conversation.getParticipantIds().contains(userId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập cuộc hội thoại này!");
        }

        // 3. Map sang DTO (Lúc này DTO sẽ có threadId)
        return conversationMapper.toDetailResponse(conversation, userId);
    }
}