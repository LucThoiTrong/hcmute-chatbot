package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    // Tìm cuộc các cuộc hội thoại theo từng mốc thời gian
    List<Conversation> findByTypeAndCreatedAtBetween(
            ConversationType type,
            LocalDateTime from,
            LocalDateTime to
    );}
