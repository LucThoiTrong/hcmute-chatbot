package hcmute.edu.vn.hcmutechatbot.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FacultyResponse {
    private String id;
    private String name;
    private List<AdvisoryDomainResponse> advisoryDomainResponse;
}
