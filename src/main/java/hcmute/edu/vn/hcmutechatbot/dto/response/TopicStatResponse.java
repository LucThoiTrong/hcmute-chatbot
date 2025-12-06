package hcmute.edu.vn.hcmutechatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicStatResponse {
    private String id;      // advisoryDomainId
    private String name;    // advisoryDomainName
    private long count;     // Số lượt chat
    private double percent; // Phần trăm hiển thị thanh bar
}