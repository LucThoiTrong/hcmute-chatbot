package hcmute.edu.vn.hcmutechatbot.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse;
import hcmute.edu.vn.hcmutechatbot.mapper.AuthMapper; // [MỚI]
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleAuthService googleAuthService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    // [MỚI] Inject thêm 2 bean này
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    public JwtResponse loginWithGoogle(String googleToken) {
        // B1: Verify Token
        GoogleIdToken.Payload payload = googleAuthService.verifyToken(googleToken);
        if (payload == null) {
            throw new RuntimeException("Token Google không hợp lệ hoặc đã hết hạn!");
        }

        // B2: Load User
        UserDetails userDetails = customUserDetailsService.loadUserByGoogleEmail(payload.getEmail());
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        // B3: Set Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // B4: Sinh Access Token
        String accessToken = jwtUtils.generateJwtToken(authentication);

        // [MỚI] B5: Sinh & Lưu Refresh Token
        String refreshToken = refreshTokenService.createRefreshToken(customUserDetails.getId(), customUserDetails.getUsername());

        // [MỚI] B6: Dùng Mapper để trả về kết quả đồng bộ với Login thường
        return authMapper.toJwtResponse(accessToken, refreshToken, customUserDetails);
    }
}