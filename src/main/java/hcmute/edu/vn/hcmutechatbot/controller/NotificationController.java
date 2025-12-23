package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.NotificationReadRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.NotificationResponse;
import hcmute.edu.vn.hcmutechatbot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    // --- 1. Lấy thông báo được nhận ---
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<NotificationResponse> notifications = notificationService.getNotificationsByUserId(page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * @param request body chứa notificationId.
     * @return Thông báo đã được cập nhật.
     */
    // --- 2. Đánh dấu đã đọc ---
    @PatchMapping("/read-one")
    public ResponseEntity<NotificationResponse> markAsRead(
            @RequestBody NotificationReadRequest request
    ) {
        NotificationResponse updatedNotification = notificationService.markNotificationAsRead(request.getNotificationId());
        return ResponseEntity.ok(updatedNotification);
    }

    // --- 3. [QUẢN LÝ] Xem lịch sử thông báo đã gửi (Chỉ FACULTY_HEAD) ---
    @PreAuthorize("hasRole('FACULTY_HEAD')")
    @GetMapping("/sent")
    public ResponseEntity<Page<NotificationResponse>> getSentNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<NotificationResponse> sentNotifications = notificationService.getSentNotifications(page, size);
        return ResponseEntity.ok(sentNotifications);
    }
}
