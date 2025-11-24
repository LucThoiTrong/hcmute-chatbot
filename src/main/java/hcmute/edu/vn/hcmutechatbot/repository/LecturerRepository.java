package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LecturerRepository extends MongoRepository<Lecturer, String> {
}
