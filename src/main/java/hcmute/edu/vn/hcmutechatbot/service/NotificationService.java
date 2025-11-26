package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.NotificationResponse;
import hcmute.edu.vn.hcmutechatbot.mapper.NotificationMapper;
import hcmute.edu.vn.hcmutechatbot.model.CourseClass;
import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import hcmute.edu.vn.hcmutechatbot.model.Notification;
import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import hcmute.edu.vn.hcmutechatbot.repository.CourseClassRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.NotificationRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
import hcmute.edu.vn.hcmutechatbot.security.ISecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements ISecurityService {
    private final NotificationRepository notificationRepository;
    private final CourseClassRepository courseClassRepository;
    private final NotificationMapper notificationMapper;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    private String getUserFacultyId(String userId, List<String> roles) {
        if (userId == null || roles == null) {
            return null;
        }

        if (roles.contains("ROLE_STUDENT")) {
            // Tìm Student theo studentId
            return studentRepository.findById(userId)
                    .map(student -> student.getAcademicInfo().getFacultyId())
                    .orElse(null);

        } else if (roles.contains("ROLE_LECTURER")) {
            // Tìm Lecturer theo id
            return lecturerRepository.findById(userId)
                    .map(Lecturer::getFacultyId)
                    .orElse(null);
        }

        return null;
    }

    // Lấy danh sách thông báo thuộc về người dùng
    public Page<NotificationResponse> getNotificationsByUserId(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        String userId = getCurrentUserId();
        List<String> roles = getAuthorities();

        if (userId == null || roles == null) {
            return Page.empty(pageable);
        }

        String userFacultyId = getUserFacultyId(userId, roles);

        if (userFacultyId == null) {
            return Page.empty(pageable);
        }

        // B2: Khởi tạo các tham số cần thiết
        Set<String> individualAndClassTargetIds;
        List<NotificationScope> facultyScopes;

        // B3: Xác định các tham số dựa trên vai trò
        if (roles.contains("ROLE_STUDENT")) {
            NotificationParams params = getStudentNotificationParams(userId);
            individualAndClassTargetIds = params.individualAndClassTargetIds();
            facultyScopes = params.facultyScopes();

        } else if (roles.contains("ROLE_LECTURER")) {
            NotificationParams params = getLecturerNotificationParams(userId);
            individualAndClassTargetIds = params.individualAndClassTargetIds();
            facultyScopes = params.facultyScopes();

        } else {
            // Trường hợp không có vai trò phù hợp
            return Page.empty(pageable);
        }

        // B4: Truy vấn thông báo
        Page<Notification> notifications = notificationRepository.findRelevantNotifications(
                individualAndClassTargetIds,
                userFacultyId,
                facultyScopes,
                pageable
        );

        // B4: Chuyển đổi sang DTO và trả về
        return notifications.map(notification -> notificationMapper.toResponse(notification, userId));
    }

    /**
     * Helper method: Lấy các tham số cần thiết cho sinh viên
     * Flow: Global, Cá nhân, Lớp học, Khoa (dành cho sinh viên)
     */
    private NotificationParams getStudentNotificationParams(String userId) {
        // 1. TargetIds: Cá nhân + Lớp học
        Set<String> individualAndClassTargetIds = new HashSet<>(Set.of(userId)); // Bắt đầu bằng UserID (Cá nhân)

        // Lấy danh sách lớp học mà sinh viên đó đã đăng ký
        List<CourseClass> studentClasses = courseClassRepository.findByStudentIdsContains(userId);

        // Thêm ClassID vào danh sách targetId
        Set<String> classIds = studentClasses.stream()
                .map(CourseClass::getId)
                .collect(Collectors.toSet());
        individualAndClassTargetIds.addAll(classIds);

        // 2. Faculty Scopes: Khoa (All) + Khoa (Student)
        List<NotificationScope> facultyScopes = Arrays.asList(
                NotificationScope.FACULTY_ALL,
                NotificationScope.FACULTY_STUDENT
        );

        return new NotificationParams(individualAndClassTargetIds, facultyScopes);
    }

    /**
     * Helper method: Lấy các tham số cần thiết cho giảng viên
     * Flow: Global, Cá nhân, Khoa (dành cho giảng viên)
     */
    private NotificationParams getLecturerNotificationParams(String userId) {
        // 1. TargetIds: Chỉ có Cá nhân (UserID)
        Set<String> individualAndClassTargetIds = Set.of(userId);

        // 2. Faculty Scopes: Khoa (All) + Khoa (Lecturer)
        List<NotificationScope> facultyScopes = Arrays.asList(
                NotificationScope.FACULTY_ALL,
                NotificationScope.FACULTY_LECTURER
        );

        return new NotificationParams(individualAndClassTargetIds, facultyScopes);
    }

    // Class đơn giản để nhóm các tham số lại, giúp code clean hơn
    private record NotificationParams(Set<String> individualAndClassTargetIds, List<NotificationScope> facultyScopes) {
    }
}