package hcmute.edu.vn.hcmutechatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LecturerStatResponse {
    private String id;
    private String fullName;
    private String email;
    private long totalChats;
}