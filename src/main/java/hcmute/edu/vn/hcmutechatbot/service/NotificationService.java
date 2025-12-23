package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.NotificationResponse;
import hcmute.edu.vn.hcmutechatbot.exception.AccessDeniedException;
import hcmute.edu.vn.hcmutechatbot.exception.ResourceNotFoundException;
import hcmute.edu.vn.hcmutechatbot.mapper.NotificationMapper;
import hcmute.edu.vn.hcmutechatbot.model.CourseClass;
import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import hcmute.edu.vn.hcmutechatbot.model.Notification;
import hcmute.edu.vn.hcmutechatbot.model.enums.NotificationScope;
import hcmute.edu.vn.hcmutechatbot.repository.*;
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

    // --- Helper Method: Lấy tên người gửi ---
    private String getSenderName(String senderId) {
        if (senderId == null) {
            return "Unknown Sender";
        }

        // 1. Tìm trong danh sách Giảng viên trước
        Optional<Lecturer> lecturer = lecturerRepository.findById(senderId);
        if (lecturer.isPresent()) {
            return lecturer.get().getFullName();
        }

        return "Unknown Sender";
    }

    private String getUserFacultyId(String userId, List<String> roles) {
        if (userId == null || roles == null) {
            return null;
        }

        if (roles.contains("ROLE_STUDENT")) {
            return studentRepository.findById(userId)
                    .map(student -> student.getAcademicInfo().getFacultyId())
                    .orElse(null);

        } else if (roles.contains("ROLE_LECTURER")) {
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

        Set<String> individualAndClassTargetIds;
        List<NotificationScope> facultyScopes;

        if (roles.contains("ROLE_STUDENT")) {
            NotificationParams params = getStudentNotificationParams(userId);
            individualAndClassTargetIds = params.individualAndClassTargetIds();
            facultyScopes = params.facultyScopes();

        } else if (roles.contains("ROLE_LECTURER")) {
            NotificationParams params = getLecturerNotificationParams(userId);
            individualAndClassTargetIds = params.individualAndClassTargetIds();
            facultyScopes = params.facultyScopes();

        } else {
            return Page.empty(pageable);
        }

        Page<Notification> notifications = notificationRepository.findRelevantNotifications(
                individualAndClassTargetIds,
                userFacultyId,
                facultyScopes,
                pageable
        );

        // Map data kèm theo SenderName
        return notifications.map(notification ->
                notificationMapper.toResponse(
                        notification,
                        userId,
                        getSenderName(notification.getSenderId())
                )
        );
    }

    private NotificationParams getStudentNotificationParams(String userId) {
        Set<String> individualAndClassTargetIds = new HashSet<>(Set.of(userId));

        List<CourseClass> studentClasses = courseClassRepository.findByStudentIdsContains(userId);

        Set<String> classIds = studentClasses.stream()
                .map(CourseClass::getId)
                .collect(Collectors.toSet());
        individualAndClassTargetIds.addAll(classIds);

        List<NotificationScope> facultyScopes = Arrays.asList(
                NotificationScope.FACULTY_ALL,
                NotificationScope.FACULTY_STUDENT
        );

        return new NotificationParams(individualAndClassTargetIds, facultyScopes);
    }

    private NotificationParams getLecturerNotificationParams(String userId) {
        Set<String> individualAndClassTargetIds = Set.of(userId);

        List<NotificationScope> facultyScopes = Arrays.asList(
                NotificationScope.FACULTY_ALL,
                NotificationScope.FACULTY_LECTURER
        );

        return new NotificationParams(individualAndClassTargetIds, facultyScopes);
    }

    private record NotificationParams(Set<String> individualAndClassTargetIds, List<NotificationScope> facultyScopes) {
    }

    public NotificationResponse markNotificationAsRead(String notificationId) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("Lỗi xác thực: Người dùng chưa đăng nhập hoặc không xác định.");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));

        Set<String> readByUsers = notification.getReadByUserIds();
        boolean wasModified = readByUsers.add(userId);

        if (wasModified) {
            notification.setReadByUserIds(readByUsers);
            notification = notificationRepository.save(notification);
        }

        String senderName = getSenderName(notification.getSenderId());
        return notificationMapper.toResponse(notification, userId, senderName);
    }

    public Page<NotificationResponse> getSentNotifications(int page, int size) {
        String userId = getCurrentUserId();
        if (userId == null) return Page.empty();

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        Page<Notification> notifications = notificationRepository.findBySenderId(userId, pageable);

        return notifications.map(notification ->
                notificationMapper.toResponse(
                        notification,
                        userId,
                        getSenderName(notification.getSenderId())
                )
        );
    }
}