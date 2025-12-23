package hcmute.edu.vn.hcmutechatbot.model;

import hcmute.edu.vn.hcmutechatbot.model.enums.FacultyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "faculties")
public class Faculty {
    @Id
    private String id;

    private String name;
    private String description;

    private FacultyType type;

    // Nhúng danh sách Ngành vào Khoa
    private Set<Major> majors;

    // Nhúng danh sách Lĩnh vực tư vấn vào Khoa
    private Set<AdvisoryDomain> advisoryDomains;
}