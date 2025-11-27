package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConsultantResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.FacultyResponse;
import hcmute.edu.vn.hcmutechatbot.mapper.ConsultantMapper;
import hcmute.edu.vn.hcmutechatbot.mapper.FacultyMapper;
import hcmute.edu.vn.hcmutechatbot.model.AdvisoryDomain;
import hcmute.edu.vn.hcmutechatbot.model.Faculty;
import hcmute.edu.vn.hcmutechatbot.repository.FacultyRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final FacultyRepository facultyRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final FacultyMapper facultyMapper;
    private final ConsultantMapper consultantMapper;

    /**
     * Lấy tất cả các Khoa/Đơn vị và các Lĩnh vực tư vấn được nhúng.
     */
    public List<FacultyResponse> getAllFaculties() {
        List<Faculty> faculties = facultyRepository.findAll();
        log.info("faculties: {}", faculties);
        return faculties.stream()
                .map(facultyMapper::toFacultyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách Tư vấn viên (Lecturer và Student) dựa trên AdvisoryDomain ID (consultantIds).
     *
     * @param advisoryDomainId ID của AdvisoryDomain (được nhúng trong Faculty)
     * @return Danh sách ConsultantResponse
     */
    public List<ConsultantResponse> getConsultantsByDomain(String advisoryDomainId) {
        // 1. Tìm Faculty chứa AdvisoryDomain đó
        Faculty faculty = facultyRepository.findByAdvisoryDomainsId(advisoryDomainId)
                .orElseThrow(() -> new RuntimeException("Advisory Domain not found"));

        // 2. Lấy Set of consultantIds từ AdvisoryDomain tương ứng
        AdvisoryDomain domain = faculty.getAdvisoryDomains().stream()
                .filter(d -> d.getId().equals(advisoryDomainId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Advisory Domain not found in Faculty"));

        Set<String> consultantIds = domain.getConsultantIds();
        if (consultantIds == null || consultantIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ConsultantResponse> consultants = new ArrayList<>();

        // 3. Tra cứu Lecturer và Student bằng consultantIds

        // Giả định: IDs của Lecturer và Student không trùng nhau

        // Lấy Lecturers
        List<String> lecturerIds = new ArrayList<>(consultantIds); // Giả sử tất cả IDs đều có thể là Lecturer IDs
        lecturerRepository.findAllById(lecturerIds).stream()
                .map(consultantMapper::toLecturerConsultantResponse)
                .forEach(consultants::add);

        // Lấy Students (Lấy những IDs còn lại hoặc tìm kiếm lại)
        List<String> studentIds = new ArrayList<>(consultantIds); // Giả sử tất cả IDs đều có thể là Student IDs
        studentRepository.findAllById(studentIds).stream()
                .map(consultantMapper::toStudentConsultantResponse)
                .forEach(consultants::add);

        // LƯU Ý: Nếu một ID có thể là cả Lecturer và Student, logic này cần được điều chỉnh để phân biệt loại ID.
        // Hiện tại, ta sẽ chấp nhận tìm kiếm ID trong cả 2 collections.

        // 4. Trả về danh sách ConsultantResponses (có thể cần distinct nếu ID trùng tên)
        return consultants.stream()
                .distinct() // Đảm bảo không có trùng lặp nếu có ID trùng tên/nhầm lẫn
                .collect(Collectors.toList());
    }
}