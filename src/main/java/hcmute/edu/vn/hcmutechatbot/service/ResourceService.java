package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConsultantResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.FacultyResponse;
import hcmute.edu.vn.hcmutechatbot.exception.ResourceNotFoundException;
import hcmute.edu.vn.hcmutechatbot.mapper.ConsultantMapper;
import hcmute.edu.vn.hcmutechatbot.mapper.FacultyMapper;
import hcmute.edu.vn.hcmutechatbot.model.AdvisoryDomain;
import hcmute.edu.vn.hcmutechatbot.model.Faculty;
import hcmute.edu.vn.hcmutechatbot.repository.FacultyRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
                .orElseThrow(() -> new ResourceNotFoundException("Advisory Domain not found"));

        // 2. Lấy Set of consultantIds từ AdvisoryDomain tương ứng
        AdvisoryDomain domain = faculty.getAdvisoryDomains().stream()
                .filter(d -> d.getId().equals(advisoryDomainId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Advisory Domain not found in Faculty"));

        Set<String> consultantIds = domain.getConsultantIds();
        if (consultantIds == null || consultantIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ConsultantResponse> consultants = new ArrayList<>();

        // 3. Tra cứu Lecturer và Student bằng consultantIds
        // Lấy Lecturers
        List<String> lecturerIds = new ArrayList<>(consultantIds);
        lecturerRepository.findAllById(lecturerIds).stream()
                .map(consultantMapper::toLecturerConsultantResponse)
                .forEach(consultants::add);
        // Lấy Students
        List<String> studentIds = new ArrayList<>(consultantIds);
        studentRepository.findAllById(studentIds).stream()
                .map(consultantMapper::toStudentConsultantResponse)
                .forEach(consultants::add);

        // 4. Trả về danh sách ConsultantResponses
        return consultants.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}