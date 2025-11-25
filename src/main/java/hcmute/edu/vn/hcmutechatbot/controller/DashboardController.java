package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.response.AdvisoryTrendResponse;
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
}
