package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.CourseClass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseClassRepository extends MongoRepository<CourseClass, String> {
    /**
     * Lấy danh sách các lớp học phần mà sinh viên có mã 'studentId' đã đăng ký.
     * Hàm này sử dụng từ khóa 'findByStudentIdsContains' của Spring Data MongoDB để
     * tìm kiếm các document có Set<String> studentIds chứa giá trị studentId.
     * @param studentId Mã số sinh viên
     * @return Danh sách các đối tượng CourseClass
     */
    List<CourseClass> findByStudentIdsContains(String studentId);
}
