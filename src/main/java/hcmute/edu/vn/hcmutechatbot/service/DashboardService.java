package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.LecturerStatResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.TopicStatResponse;
import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import hcmute.edu.vn.hcmutechatbot.repository.ConversationRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.security.ISecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService implements ISecurityService {

    private final ConversationRepository conversationRepository;
    private final LecturerRepository lecturerRepository;

    private String getCurrentFacultyId() {
        // 1. Lấy ID user từ token/session (qua ISecurityService)
        String userId = getCurrentUserId();

        // 2. Query DB Lecturer để tìm thông tin giảng viên
        Lecturer lecturer = lecturerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên với ID: " + userId));

        // 3. Lấy Faculty ID
        if (lecturer.getFacultyId() == null) {
            throw new RuntimeException("Giảng viên chưa thuộc về Khoa nào.");
        }
        return lecturer.getFacultyId();
    }

    // --- Chức năng 1: Count số sinh viên (Distinct) ---
    public long countTotalStudentsForMyFaculty() {
        String facultyId = getCurrentFacultyId();
        Long count = conversationRepository.countDistinctStudentsByFaculty(facultyId);
        return count != null ? count : 0L; // Aggregation trả null nếu không có data
    }

    // --- Chức năng 2: Count tổng số cuộc hội thoại ---
    public long countTotalConversationsForMyFaculty() {
        String facultyId = getCurrentFacultyId();
        return conversationRepository.countByFacultyId(facultyId);
    }

    public List<TopicStatResponse> getTopTopicsForMyFaculty() {
        String facultyId = getCurrentFacultyId();
        // 1. Lấy dữ liệu thô từ Aggregation
        List<TopicStatResponse> topics = conversationRepository.getTopTrendingTopics(facultyId);
        if (topics.isEmpty()) {
            return topics;
        }
        // 2. Tính tổng số cuộc hội thoại để tính %
        long totalConversations = conversationRepository.countByFacultyId(facultyId);
        // 3. Tính percent
        if (totalConversations > 0) {
            topics.forEach(topic -> {
                double rawPercent = ((double) topic.getCount() / totalConversations) * 100;
                // Làm tròn 1 chữ số thập phân (VD: 35.5)
                topic.setPercent(Math.round(rawPercent * 10.0) / 10.0);
            });
        }
        return topics;
    }

    // Trong class DashboardService
    public Page<LecturerStatResponse> getLecturersList(String keyword, int page, int size) {
        String facultyId = getCurrentFacultyId();

        // 1. Phân trang & Sort theo tên
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("fullName").ascending());

        // 2. Query DB
        String searchKey = (keyword == null) ? "" : keyword.trim();
        Page<Lecturer> lecturerPage = lecturerRepository.findByFacultyIdAndFullNameContainingIgnoreCase(facultyId, searchKey, pageable);

        // 3. Map sang DTO
        return lecturerPage.map(lecturer -> {
            // Đếm số chat
            long chatCount = conversationRepository.countByParticipantIdsContains(lecturer.getId());

            return LecturerStatResponse.builder()
                    .id(lecturer.getId())
                    .fullName(lecturer.getFullName())
                    .email(lecturer.getEmail())
                    .totalChats(chatCount)
                    .build();
        });
    }
}