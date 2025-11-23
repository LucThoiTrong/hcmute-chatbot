package hcmute.edu.vn.hcmutechatbot.model;

import hcmute.edu.vn.hcmutechatbot.model.enums.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramSubject {
    private String courseId;
    private String courseName;

    private SubjectType subjectType;
    private int credits;
    private int semester; // Học kỳ dự kiến (HK1, HK2...)
}