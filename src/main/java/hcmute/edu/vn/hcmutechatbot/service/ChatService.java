package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.MessageResponse;
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
    public Page<ConversationResponse> getConversationsByUserId(int page, int size) {
        String userId = getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdatedAt").descending());

        // Truyền userId vào cả 2 tham số:
        // - Tham số 1: Để tìm user trong danh sách tham gia
        // - Tham số 2: Để đảm bảo user KHÔNG nằm trong danh sách đã xóa
        Page<Conversation> conversationPage = conversationRepository.findByParticipantIdsContainsAndDeletedByUserIdsNotContains(
                userId, // participantId
                userId, // deletedUserId
                pageable
        );

        return conversationPage.map(conversation -> conversationMapper.toResponse(conversation, userId));
    }

    // 2. Hàm Patch cập nhật tiêu đề cuộc hội thoại (userTitles)
    public ConversationResponse updateConversationTitle(String conversationId, String newTitle) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        String userId = getCurrentUserId();

        // Kiểm tra xem user có trong cuộc hội thoại không
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new RuntimeException("User is not a participant of this conversation");
        }

        // Cập nhật title riêng cho user này
        conversation.getUserTitles().put(userId, newTitle);

        Conversation updatedConversation = conversationRepository.save(conversation);

        return conversationMapper.toResponse(updatedConversation, userId);
    }

    // 3. Hàm Patch cập nhật xóa mềm (deletedByUserIds)
    public void softDeleteConversation(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

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
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 2. Kiểm tra quyền truy cập (User phải có trong participantIds)
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập cuộc hội thoại này");
        }

        // 3. Query DB lấy tin nhắn (Sắp xếp MỚI NHẤT lên đầu để lazy load đúng chuẩn)
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<Message> messagePage = messageRepository.findByConversationId(conversationId, pageable);

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
            String displayName = null;

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
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 2. Security Check: Người dùng có nằm trong cuộc hội thoại này không?
        // Nếu không có dòng này, ai biết ID cũng xem được trộm tin nhắn -> Lỗi bảo mật to
        if (conversation.getParticipantIds() == null || !conversation.getParticipantIds().contains(userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập cuộc hội thoại này");
        }

        // 3. Map sang DTO (Lúc này DTO sẽ có threadId)
        return conversationMapper.toResponse(conversation, userId);
    }
}