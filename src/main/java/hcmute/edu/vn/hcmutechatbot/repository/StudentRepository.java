package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<Student, String> {
}
