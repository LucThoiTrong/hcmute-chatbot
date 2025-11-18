package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.StudentCredential;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentCredentialRepository extends MongoRepository<StudentCredential, String> {
}
