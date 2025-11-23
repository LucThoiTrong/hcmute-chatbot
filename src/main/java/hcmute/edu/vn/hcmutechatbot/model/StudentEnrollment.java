package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "enrollments")
public class StudentEnrollment {

    @Id
    private String id;

    @Indexed
    private String studentId;

    @Indexed
    private String courseClassId;

    @Indexed // Index để query theo kỳ học cho nhanh
    private String semester;     // Ví dụ: HK1_2024_2025

    private String academicYear; // Ví dụ: 2024-2025

    private Double midtermScore;
    private Double finalScore;
    private Double totalScore;
}