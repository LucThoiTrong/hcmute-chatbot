package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByConversationId(String conversationId, Pageable pageable);
}
