package hcmute.edu.vn.hcmutechatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisoryTrendResponse {
    private String advisoryDomainName;
    private String facultyName;
    private Long count;
}