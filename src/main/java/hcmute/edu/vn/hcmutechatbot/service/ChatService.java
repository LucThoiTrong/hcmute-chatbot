package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.mapper.ConversationMapper;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import hcmute.edu.vn.hcmutechatbot.security.ISecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ChatService implements ISecurityService {
    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;

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
}