package hcmute.edu.vn.hcmutechatbot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultantResponse {
    private String id;
    private String fullName;
}
