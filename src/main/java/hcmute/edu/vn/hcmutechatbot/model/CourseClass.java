package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "course_classes")
public class CourseClass {

    @Id
    private String id; // Mã lớp học phần

    private String name; // Tên lớp (Ví dụ: 01CLC)

    @Indexed
    private String courseId; // Link tới môn học
    private String courseName;

    private String lecturerId;
    private String lecturerName;

    private String semester;     // HK1_2024_2025
    private String academicYear; // 2024-2025

    // Danh sách SV trong lớp
    private Set<String> studentIds;

    // Nhúng lịch học (1 lớp có thể học nhiều buổi/tuần)
    private Set<TimeSlot> timeSlots;
}