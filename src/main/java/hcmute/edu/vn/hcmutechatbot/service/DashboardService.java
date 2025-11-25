package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.AdvisoryTrendResponse;
import hcmute.edu.vn.hcmutechatbot.model.Conversation;
import hcmute.edu.vn.hcmutechatbot.model.enums.ConversationType;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
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
}