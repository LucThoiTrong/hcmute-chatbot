package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturerRepository extends MongoRepository<Lecturer, String> {
    Page<Lecturer> findByFacultyIdAndFullNameContainingIgnoreCase(String facultyId, String fullName, Pageable pageable);
}
