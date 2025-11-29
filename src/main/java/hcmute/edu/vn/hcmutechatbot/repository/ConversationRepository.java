package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    // Tìm cuộc các cuộc hội thoại theo từng mốc thời gian
    List<Conversation> findByTypeAndCreatedAtBetween(
            ConversationType type,
            LocalDateTime from,
            LocalDateTime to
    );

    Page<Conversation> findByParticipantIdsContainsAndDeletedByUserIdsNotContains(
            String participantId,
            String deletedUserId,
            Pageable pageable
    );

    // Đếm tổng số conversation trong khoảng thời gian
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Đếm tổng số conversation theo type trong khoảng thời gian
    long countByTypeAndCreatedAtBetween(ConversationType type, LocalDateTime start, LocalDateTime end);

    // Lấy danh sách conversation trong khoảng thời gian
    List<Conversation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
