package hcmute.edu.vn.hcmutechatbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private String activityName; // tenHoatDong
    private String description; // moTa
    private int activityPoints; // diemHoatDong
    private ActivityType activityType; // loaiHoatDong
    private String recordedBy; // nguoiNhap
}