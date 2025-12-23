package hcmute.edu.vn.hcmutechatbot.model;

import hcmute.edu.vn.hcmutechatbot.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "students")
public class Student {
    @Id
    private String studentId; // Mapping field này làm _id của MongoDB luôn

    private String fullName;
    private LocalDate birthDate;
    private String birthPlace;

    private Gender gender;

    private String citizenId; // CCCD/CMND
    private String ethnicity; // Dân tộc
    private String religion;  // Tôn giáo

    // Embedding (Nhúng trực tiếp 3 object con vào)
    private ContactInfo contactInfo;

    private ContactPerson contactPerson;

    private AcademicInfo academicInfo;
}