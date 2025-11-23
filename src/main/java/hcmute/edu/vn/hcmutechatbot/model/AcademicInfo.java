package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcademicInfo {
    private String cohort; // Khóa học
    private LocalDate admissionDate; // Ngày nhập học
    private String trainingType; // Hệ đào tạo (Đại trà, CLC...)
    private String trainingProgram;
    private String academicYear;
    private String trainingEndYear;

    // Thông tin khoa/ngành (Lưu cả ID và Name để không cần phải join)
    private String facultyId;
    private String facultyName;

    private String majorId;
    private String majorName;

    private String specializationId;
    private String specializationName;
}