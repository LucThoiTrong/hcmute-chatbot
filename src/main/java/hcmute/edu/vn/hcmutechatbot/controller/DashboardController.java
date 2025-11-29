package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.response.AdvisoryTrendResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.FacultyStatisticResponse;
import hcmute.edu.vn.hcmutechatbot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/top-trends")
    public ResponseEntity<List<AdvisoryTrendResponse>> getTopTrends(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        List<AdvisoryTrendResponse> result = dashboardService.getTop5AdvisoryTrends(month, year);

        return ResponseEntity.ok(result);
    }


    // 1. Tổng số conversation
    @GetMapping("/count-total")
    public ResponseEntity<Long> countTotalConversations(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(dashboardService.countTotalConversations(month, year));
    }

    // 2. Thống kê theo Khoa
    @GetMapping("/stats-faculty")
    public ResponseEntity<List<FacultyStatisticResponse>> getStatsByFaculty(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(dashboardService.getConversationStatsByFaculty(month, year));
    }

    @GetMapping("/count-lookup")
    public ResponseEntity<Long> countLookupConversations(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        long count = dashboardService.countLookupConversations(month, year);
        return ResponseEntity.ok(count);
    }
}
