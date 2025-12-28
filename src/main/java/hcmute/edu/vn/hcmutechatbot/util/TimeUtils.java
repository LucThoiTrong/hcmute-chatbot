package hcmute.edu.vn.hcmutechatbot.util;
import hcmute.edu.vn.hcmutechatbot.model.TimeSlot;

import java.util.HashMap;
import java.util.Map;

public class TimeUtils {

    // Map tiết bắt đầu -> Giờ bắt đầu
    private static final Map<Integer, String> START_TIMES = new HashMap<>();
    // Map tiết kết thúc -> Giờ kết thúc
    private static final Map<Integer, String> END_TIMES = new HashMap<>();

    static {
        // Cấu hình khung giờ HCMUTE (HK1 2025-2026)
        // Sáng
        setupTime(1, "07:30", "08:15");
        setupTime(2, "08:15", "09:00");
        setupTime(3, "09:00", "09:45");
        setupTime(4, "10:00", "10:45");
        setupTime(5, "10:45", "11:30");
        setupTime(6, "11:30", "12:15");
        // Chiều
        setupTime(7, "12:45", "13:30");
        setupTime(8, "13:30", "14:15");
        setupTime(9, "14:15", "15:00");
        setupTime(10, "15:15", "16:00");
        setupTime(11, "16:00", "16:45");
        setupTime(12, "16:45", "17:30");
        // Tối
        setupTime(13, "18:00", "18:45");
        setupTime(14, "18:45", "19:30");
        setupTime(15, "19:30", "20:15");
        setupTime(16, "20:15", "21:00");
    }

    private static void setupTime(int period, String start, String end) {
        START_TIMES.put(period, start);
        END_TIMES.put(period, end);
    }

    public static void enrichTime(TimeSlot slot) {
        if (slot == null) return;

        String start = START_TIMES.getOrDefault(slot.getStartPeriod(), "??:??");
        String end = END_TIMES.getOrDefault(slot.getEndPeriod(), "??:??");

        slot.setStartTime(start);
        slot.setEndTime(end);
    }
}