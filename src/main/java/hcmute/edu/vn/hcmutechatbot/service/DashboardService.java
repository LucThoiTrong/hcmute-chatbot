package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.AdvisoryTrendResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.FacultyStatisticResponse;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ConversationRepository conversationRepository;

    public List<AdvisoryTrendResponse> getTop5AdvisoryTrends(Integer month, Integer year) {

        LocalDateTime startDate;
        LocalDateTime endDate;

        int targetYear = (year != null) ? year : LocalDateTime.now().getYear();

        if (month != null && month > 0 && month <= 12) {
            // CASE 1: LỌC THEO THÁNG CỤ THỂ
            YearMonth yearMonth = YearMonth.of(targetYear, month);
            startDate = yearMonth.atDay(1).atStartOfDay(); // Ngày 1 lúc 00:00:00
            endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59); // Ngày cuối lúc 23:59:59
        } else {
            // CASE 2: LẤY CẢ NĂM (ALL 12 THÁNG)
            startDate = LocalDateTime.of(targetYear, 1, 1, 0, 0, 0);
            endDate = LocalDateTime.of(targetYear, 12, 31, 23, 59, 59);
        }

        // 2. QUERY DATABASE (Lọc Type + Thời gian ngay tại DB cho nhẹ)
        List<Conversation> conversations = conversationRepository.findByTypeAndCreatedAtBetween(
                ConversationType.ADVISORY,
                startDate,
                endDate
        );

        // 3. XỬ LÝ JAVA STREAM
        return conversations.stream()
                .collect(Collectors.groupingBy(Conversation::getAdvisoryDomainId))
                .values().stream()
                .map(group -> {
                    Conversation firstItem = group.getFirst();

                    String dName = firstItem.getAdvisoryDomainName() != null ? firstItem.getAdvisoryDomainName() : "Unknown";
                    String fName = firstItem.getFacultyName() != null ? firstItem.getFacultyName() : "Unknown";

                    return AdvisoryTrendResponse.builder()
                            .advisoryDomainName(dName)
                            .facultyName(fName)
                            .count((long) group.size())
                            .build();
                })
                .sorted((o1, o2) -> Long.compare(o2.getCount(), o1.getCount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public long countTotalConversations(Integer month, Integer year) {
        LocalDateTime[] range = getDateRange(month, year);
        return conversationRepository.countByCreatedAtBetween(range[0], range[1]);
    }

    public List<FacultyStatisticResponse> getConversationStatsByFaculty(Integer month, Integer year) {
        LocalDateTime[] range = getDateRange(month, year);

        // Lấy hết conversation trong khoảng thời gian
        List<Conversation> conversations = conversationRepository.findByCreatedAtBetween(range[0], range[1]);

        // Group theo Faculty Name và Count
        Map<String, Long> stats = conversations.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getFacultyName() != null ? c.getFacultyName() : "Chưa xác định",
                        Collectors.counting()
                ));

        // Map sang DTO Response
        return stats.entrySet().stream()
                .map(entry -> FacultyStatisticResponse.builder()
                        .facultyName(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted((o1, o2) -> Long.compare(o2.getCount(), o1.getCount())) // Sort giảm dần
                .collect(Collectors.toList());
    }

    public long countLookupConversations(Integer month, Integer year) {
        LocalDateTime[] range = getDateRange(month, year);

        return conversationRepository.countByTypeAndCreatedAtBetween(
                ConversationType.LOOKUP,
                range[0],
                range[1]
        );
    }

    private LocalDateTime[] getDateRange(Integer month, Integer year) {
        int targetYear = (year != null) ? year : LocalDateTime.now().getYear();
        LocalDateTime startDate;
        LocalDateTime endDate;

        if (month != null && month > 0 && month <= 12) {
            YearMonth yearMonth = YearMonth.of(targetYear, month);
            startDate = yearMonth.atDay(1).atStartOfDay();
            endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        } else {
            startDate = LocalDateTime.of(targetYear, 1, 1, 0, 0, 0);
            endDate = LocalDateTime.of(targetYear, 12, 31, 23, 59, 59);
        }
        return new LocalDateTime[]{startDate, endDate};
    }
}