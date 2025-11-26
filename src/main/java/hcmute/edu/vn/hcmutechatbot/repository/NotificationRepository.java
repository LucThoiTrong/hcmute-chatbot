package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Notification;
import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // ?0: individualAndClassTargetIds (UserID, ClassIDs)
    // ?1: userFacultyId
    // ?2: facultyScopes (["FACULTY_STUDENT", "FACULTY_ALL"])
    @Query("{ '$or': [ " +
            "   { 'scope': 'GLOBAL' }, " +
            "   { 'scope': { '$in': ['INDIVIDUAL', 'CLASS'] }, 'targetId': { '$in': ?0 } }, " +
            "   { 'scope': { '$in': ?2 }, 'targetId': ?1 } " +
            "] }")
    Page<Notification> findRelevantNotifications(
            Set<String> individualAndClassTargetIds,
            String userFacultyId,
            List<NotificationScope> facultyScopes,
            Pageable pageable
    );
}
