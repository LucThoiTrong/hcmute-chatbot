package hcmute.edu.vn.hcmutechatbot.model;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicInfo {
    private String courseName; // khoaHoc
    private LocalDate enrollmentDate; // ngayNhapHoc
    private String trainingType; // loaiHinhDaoTao
    private String trainingProgram; // chuongTrinhDaoTao
    private String faculty; // khoa
    private String major; // nganh
    private String academicYear; // nienKhoa
    private String graduationYear; // namHetThoiGianDaoTao
}
