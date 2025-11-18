package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private String dayOfWeek; // thu
    private String startTime; // tietBatDau (Tôi giả định là tiết/giờ bắt đầu)
    private String endTime; // tietKetThuc
    private String studyWeeks; // tuanHoc (Tuần học)
    private Location location; // diaDiem
}
