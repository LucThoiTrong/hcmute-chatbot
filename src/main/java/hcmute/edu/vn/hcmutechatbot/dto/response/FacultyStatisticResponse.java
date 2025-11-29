package hcmute.edu.vn.hcmutechatbot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyStatisticResponse {
    private String facultyName;
    private Long count;
}