package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.request.CreateNotificationRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.NotificationResponse;
import hcmute.edu.vn.hcmutechatbot.mapper.NotificationMapper;
import hcmute.edu.vn.hcmutechatbot.model.Notification;
import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.NotificationRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
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
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public void createAndSendNotification(CreateNotificationRequest request, Principal principal) {
        // 1. Lấy thông tin User hiện tại từ Principal (An toàn cho WebSocket)
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String senderId = user.getUsername();

        log.info("Processing notification from user: {}", senderId);

        // 2. Xác định Faculty ID của người gửi (Để biết bắn vào Topic khoa nào)
        String facultyId = getSenderFacultyId(senderId);

        // --- [QUAN TRỌNG] FIX LOGIC TARGET ID ---
        // Mặc định targetId lấy từ request (cho trường hợp gửi cá nhân)
        String finalTargetId = request.getTargetId();

        // Nếu gửi cho Khoa (Student/Lecturer/All), bắt buộc targetId phải là Mã Khoa của người gửi
        if (request.getScope() == NotificationScope.FACULTY_ALL ||
                request.getScope() == NotificationScope.FACULTY_STUDENT ||
                request.getScope() == NotificationScope.FACULTY_LECTURER) {

            finalTargetId = facultyId; // Tự động gán mã khoa (VD: "F_IT") vào targetId
        }

        // 3. Lưu vào Database
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .senderId(senderId)
                .scope(request.getScope())
                .targetId(finalTargetId) // Lưu targetId đã xử lý chuẩn vào DB
                .timestamp(LocalDateTime.now())
                .readByUserIds(new HashSet<>())
                .build();

        Notification savedNoti = notificationRepository.save(notification);

        // 4. Map sang Response
        NotificationResponse response = notificationMapper.toResponse(savedNoti, null);

        // 5. Gửi WebSocket Realtime
        // Lưu ý: facultyId ở đây dùng để tạo Topic string (VD: topic/faculty.F_IT.all)
        pushNotificationToClients(savedNoti, response, facultyId);

        log.info("Notification created and pushed to clients. ID: {}, Target: {}", savedNoti.getId(), finalTargetId);
    }

    /**
     * Helper: Tìm FacultyId dựa trên ID người gửi (Student hoặc Lecturer)
     */
    private String getSenderFacultyId(String userId) {
        // Ưu tiên check Lecturer trước (Thường giảng viên/QTV khoa hay gửi thông báo)
        var lecturerOpt = lecturerRepository.findById(userId);
        if (lecturerOpt.isPresent()) {
            return lecturerOpt.get().getFacultyId();
        }

        // Check Student (nếu sinh viên được quyền tạo thông báo)
        var studentOpt = studentRepository.findById(userId);
        if (studentOpt.isPresent()) {
            return studentOpt.get().getAcademicInfo().getFacultyId();
        }

        // Fallback: Admin hoặc User không thuộc khoa nào -> Trả về "ALL"
        return "ALL";
    }

    /**
     * Helper: Logic bắn socket (Dùng dấu chấm . cho Topic chuẩn RabbitMQ)
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
                    // Topic: /topic/faculty.F_IT.all
                    messagingTemplate.convertAndSend("/topic/faculty." + facultyId + ".all", response);
                    break;

                case FACULTY_STUDENT:
                    // Topic: /topic/faculty.F_IT.student
                    messagingTemplate.convertAndSend("/topic/faculty." + facultyId + ".student", response);
                    break;

                case FACULTY_LECTURER:
                    // Topic: /topic/faculty.F_IT.lecturer
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