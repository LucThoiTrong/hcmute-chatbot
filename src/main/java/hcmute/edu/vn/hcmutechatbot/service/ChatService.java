package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConversationResponse;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;

    // 1. Lấy danh sách chat
    public Page<ConversationResponse> getConversationsByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdatedAt").descending());

        // Truyền userId vào cả 2 tham số:
        // - Tham số 1: Để tìm user trong danh sách tham gia
        // - Tham số 2: Để đảm bảo user KHÔNG nằm trong danh sách đã xóa
        Page<Conversation> conversationPage = conversationRepository.findByParticipantIdsContainsAndDeletedByUserIdsNotContains(
                userId, // participantId
                userId, // deletedUserId
                pageable
        );

        return conversationPage.map(conversation -> ConversationResponse.from(conversation, userId));
    }

    // 2. Hàm Patch cập nhật tiêu đề cuộc hội thoại (userTitles)
    public ConversationResponse updateConversationTitle(String conversationId, String userId, String newTitle) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Kiểm tra xem user có trong cuộc hội thoại không
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new RuntimeException("User is not a participant of this conversation");
        }

        // Cập nhật title riêng cho user này
        conversation.getUserTitles().put(userId, newTitle);

        Conversation updatedConversation = conversationRepository.save(conversation);

        return ConversationResponse.from(updatedConversation, userId);
    }

    // 3. Hàm Patch cập nhật xóa mềm (deletedByUserIds)
    public void softDeleteConversation(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getDeletedByUserIds() == null) {
            conversation.setDeletedByUserIds(new HashSet<>());
        }

        // Thêm userId vào set deleted (Set sẽ tự loại bỏ trùng lặp)
        conversation.getDeletedByUserIds().add(userId);

        conversationRepository.save(conversation);
    }
}