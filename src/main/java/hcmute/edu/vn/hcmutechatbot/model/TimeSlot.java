package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {
    private String dayOfWeek; // "MONDAY", "TUESDAY"... hoặc "Thứ 2"
    private int startPeriod;  // Tiết bắt đầu (1-12)
    private int endPeriod;    // Tiết kết thúc
    private String room;
    private String campus;    // Cơ sở 1, Cơ sở 2...
    private String startTime;
    private String endTime;
}