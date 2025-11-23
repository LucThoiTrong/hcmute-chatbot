package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "courses")
public class Course {

    @Id
    private String id; // Mã môn học (INT1340...)

    private String name;
    private String description;

    private String facultyId; // Khoa quản lý môn này

    // Danh sách ID các giảng viên dạy môn này
    private Set<String> lecturers;
}