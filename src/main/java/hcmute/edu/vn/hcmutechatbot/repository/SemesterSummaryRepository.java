package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.SemesterSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SemesterSummaryRepository extends MongoRepository<SemesterSummary, String> {
}
