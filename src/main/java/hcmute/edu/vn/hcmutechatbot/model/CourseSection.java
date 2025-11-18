package hcmute.edu.vn.hcmutechatbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSection {
    private Subject subject; // monHoc
    private String mainLecturer; // giangVien
    private double midTermScore; // diemQuaTrinh
    private double finalScore; // diemKetThucHocPhan
    private String assistantLecturer; // giangVienPhuTrach
    private Set<Schedule> scheduleList; // dsLichHoc
}