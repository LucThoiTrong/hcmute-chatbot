package hcmute.edu.vn.hcmutechatbot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvisoryDomainResponse {
    private String id;
    private String name;
    private String description;
}
