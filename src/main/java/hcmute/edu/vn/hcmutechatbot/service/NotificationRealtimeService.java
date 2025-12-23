package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.request.CreateNotificationRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.NotificationResponse;
import hcmute.edu.vn.hcmutechatbot.mapper.NotificationMapper;
import hcmute.edu.vn.hcmutechatbot.model.Notification;
import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.NotificationRepository;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRealtimeService {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final LecturerRepository lecturerRepository;

    public void createAndSendNotification(CreateNotificationRequest request, Principal principal) {
        // 1. Lấy thông tin User hiện tại từ Principal
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String senderId = user.getUsername();

        log.info("Processing notification from user: {}", senderId);

        // 2. Xác định Faculty ID của người gửi
        String facultyId = getSenderFacultyId(senderId, user);

        // Mặc định targetId lấy từ request
        String finalTargetId = request.getTargetId();

        if (request.getScope() == NotificationScope.FACULTY_ALL ||
                request.getScope() == NotificationScope.FACULTY_STUDENT ||
                request.getScope() == NotificationScope.FACULTY_LECTURER) {

            finalTargetId = facultyId;
        }

        // 3. Lưu vào Database
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .senderId(senderId)
                .scope(request.getScope())
                .targetId(finalTargetId)
                .timestamp(LocalDateTime.now())
                .readByUserIds(new HashSet<>())
                .build();

        Notification savedNoti = notificationRepository.save(notification);

        // 4. Map sang Response
        NotificationResponse response = notificationMapper.toResponse(savedNoti, null);

        // 5. Gửi WebSocket Realtime
        pushNotificationToClients(savedNoti, response, facultyId);

        log.info("Notification created and pushed to clients. ID: {}, Target: {}", savedNoti.getId(), finalTargetId);
    }

    /**
     * Helper: Tìm FacultyId dựa trên ID người gửi (Student hoặc Lecturer)
     */
    private String getSenderFacultyId(String userId, CustomUserDetails user) {
        // 1. Kiểm tra quyền bảo mật: Chỉ Trưởng khoa mới được gửi thông báo
        String roles = user.getAuthorities().toString();
        boolean isAuthorized = roles.contains("FACULTY_HEAD");

        if (!isAuthorized) {
            log.warn("User {} không có quyền gửi thông báo (phải là Giảng viên/Trưởng khoa)", userId);
            return null;
        }

        // 2. Truy vấn FacultyId từ thông tin giảng viên
        return lecturerRepository.findById(userId)
                .map(lecturer -> {
                    log.info("Found faculty {} for lecturer {}", lecturer.getFacultyId(), userId);
                    return lecturer.getFacultyId();
                })
                .orElseGet(() -> {
                    log.info("User {} là quản trị viên hoặc không tìm thấy thông tin khoa, trả về ALL", userId);
                    return "ALL";
                });
    }

    /**
     * Helper: Logic bắn socket
     */
    private void pushNotificationToClients(Notification noti, NotificationResponse response, String facultyId) {
        try {
            switch (noti.getScope()) {
                case INDIVIDUAL:
                    // Gửi riêng cho 1 người (Topic: /user/{userId}/queue/notifications)
                    if (noti.getTargetId() != null) {
                        messagingTemplate.convertAndSendToUser(
                                noti.getTargetId(),
                                "/queue/notifications",
                                response
                        );
                    }
                    break;

                case FACULTY_ALL:
                    // Topic: /topic/faculty.{facultyId}.all
                    messagingTemplate.convertAndSend("/topic/faculty." + facultyId + ".all", response);
                    break;

                case FACULTY_STUDENT:
                    // Topic: /topic/faculty.{facultyId}.student
                    messagingTemplate.convertAndSend("/topic/faculty." + facultyId + ".student", response);
                    break;

                case FACULTY_LECTURER:
                    // Topic: /topic/faculty.{facultyId}.lecturer
                    messagingTemplate.convertAndSend("/topic/faculty." + facultyId + ".lecturer", response);
                    break;

                default:
                    log.warn("Unknown scope: {}", noti.getScope());
            }
        } catch (Exception e) {
            log.error("Failed to push notification via WebSocket", e);
        }
    }
}