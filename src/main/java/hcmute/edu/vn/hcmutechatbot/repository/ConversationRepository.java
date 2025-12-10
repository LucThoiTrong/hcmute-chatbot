package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.dto.response.TopicStatResponse;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.Faculty;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Page<Conversation> findByParticipantIdsContainsAndDeletedByUserIdsNotContains(
            String participantId,
            String deletedUserId,
            Pageable pageable
    );

    Page<Conversation> findByParticipantIdsContainsAndDeletedByUserIdsNotContainsAndTitleContainingIgnoreCase(
            String participantId,
            String deletedUserId,
            String keyword,
            Pageable pageable
    );

    // 1. Đếm tổng số cuộc hội thoại của khoa (Count All)
    long countByFacultyId(String facultyId);

    // 2. Đếm số lượng sinh viên (distinct createdByUserId) đã chat với khoa (Count All)
    @Aggregation(pipeline = {
            "{ '$match': { 'facultyId': ?0 } }",
            "{ '$group': { '_id': '$createdByUserId' } }",
            "{ '$count': 'total' }"
    })
    Long countDistinctStudentsByFaculty(String facultyId);

    // 3. Lấy Top 5 chủ đề được quan tâm nhất của Khoa
    // Logic:
    // - Match: Theo facultyId và advisoryDomainId phải tồn tại
    // - Group: Theo id và name, đếm tổng (sum: 1)
    // - Project: Map các field sang tên của DTO
    // - Sort: Giảm dần theo count
    // - Limit: Lấy 5 dòng đầu
    @Aggregation(pipeline = {
            "{ '$match': { 'facultyId': ?0, 'advisoryDomainId': { '$ne': null } } }",
            "{ '$group': { '_id': { 'id': '$advisoryDomainId', 'name': '$advisoryDomainName' }, 'count': { '$sum': 1 } } }",
            "{ '$project': { 'id': '$_id.id', 'name': '$_id.name', 'count': '$count', 'percent': { '$literal': 0.0 } } }",
            "{ '$sort': { 'count': -1 } }",
            "{ '$limit': 5 }"
    })
    List<TopicStatResponse> getTopTrendingTopics(String facultyId);

    long countByParticipantIdsContains(String participantId);
}
