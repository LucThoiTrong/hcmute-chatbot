package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.response.LecturerStatResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.TopicStatResponse;
import hcmute.edu.vn.hcmutechatbot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    // API: Đếm tổng số sinh viên đã chat với khoa (All time)
    @GetMapping("/my-faculty/students/count")
    public ResponseEntity<Long> countMyFacultyStudents() {
        long count = dashboardService.countTotalStudentsForMyFaculty();
        return ResponseEntity.ok(count);
    }

    // API: Đếm tổng số cuộc hội thoại của khoa (All time)
    @GetMapping("/my-faculty/conversations/count")
    public ResponseEntity<Long> countMyFacultyConversations() {
        long count = dashboardService.countTotalConversationsForMyFaculty();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/my-faculty/topics/top")
    public ResponseEntity<List<TopicStatResponse>> getTopTopics() {
        List<TopicStatResponse> topics = dashboardService.getTopTopicsForMyFaculty();
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/my-faculty/lecturers")
    public ResponseEntity<Page<LecturerStatResponse>> getLecturers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "7") int size
    ) {
        Page<LecturerStatResponse> result = dashboardService.getLecturersList(keyword, page, size);
        return ResponseEntity.ok(result);
    }
}