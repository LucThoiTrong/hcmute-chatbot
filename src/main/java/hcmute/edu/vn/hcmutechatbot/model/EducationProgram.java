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
@Document(collection = "education_programs")
public class EducationProgram {
    @Id
    private String id;

    private String majorId;
    private String cohort;

    // Nhúng danh sách môn học của chương trình vào đây
    private Set<ProgramSubject> subjects;
}