package hcmute.edu.vn.hcmutechatbot.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse {
    private String accessToken; // Đây là Access Token

    // [MỚI] Thêm field này để trả về Refresh Token cho Client
    private String refreshToken;

    @Builder.Default
    private String type = "Bearer";

    private String id;
    private String username;
    private String fullName;
    private String ownerId;
    private List<String> roles;

    private String facultyId;
}
