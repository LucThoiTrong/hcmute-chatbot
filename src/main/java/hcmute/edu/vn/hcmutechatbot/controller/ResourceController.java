package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.response.ConsultantResponse;
import hcmute.edu.vn.hcmutechatbot.dto.response.FacultyResponse;
import hcmute.edu.vn.hcmutechatbot.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * GET /api/resources/faculties
     * Lấy danh sách các Khoa/Đơn vị (bao gồm danh sách Lĩnh vực tư vấn nhúng bên trong).
     */
    @GetMapping("/faculties")
    public ResponseEntity<List<FacultyResponse>> getAllFaculties() {
        return ResponseEntity.ok(resourceService.getAllFaculties());
    }

    /**
     * GET /api/resources/consultants?domainId=...
     * Lấy danh sách Tư vấn viên (Lecturer và Student) dựa trên AdvisoryDomain ID.
     */
    @GetMapping("/consultants")
    public ResponseEntity<List<ConsultantResponse>> getConsultants(
            @RequestParam(name = "domainId") String advisoryDomainId
    ) {
        if (advisoryDomainId == null || advisoryDomainId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(resourceService.getConsultantsByDomain(advisoryDomainId));
    }
}