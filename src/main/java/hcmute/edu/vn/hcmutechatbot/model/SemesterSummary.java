package hcmute.edu.vn.hcmutechatbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Set;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterSummary {
    @Id
    private String id;
    private String studentId;
    private String semesterDetails; // thongTinHocKy
    private double totalTuition; // tongSoTien
    private Set<CourseSection> courseSections; // dsMonHocDangKy
    private Set<Activity> activitiesParticipated; // dsCacHoatDongThamGia
    private LocalDate startDate; // ngayBatDau
    private LocalDate endDate; // ngayKetThuc
}