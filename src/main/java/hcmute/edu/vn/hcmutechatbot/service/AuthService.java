package hcmute.edu.vn.hcmutechatbot.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleAuthService googleAuthService;          // 1. Để verify token
    private final CustomUserDetailsService customUserDetailsService; // 2. Để load user (Code bạn đã có)
    private final JwtUtils jwtUtils;                            // 3. Để tạo token hệ thống

    public JwtResponse loginWithGoogle(String googleToken) {
        // BƯỚC 1: Verify Token với Google (Dùng GoogleAuthService đã có)
        GoogleIdToken.Payload payload = googleAuthService.verifyToken(googleToken);

        if (payload == null) {
            throw new RuntimeException("Token Google không hợp lệ hoặc đã hết hạn!");
        }

        String email = payload.getEmail();

        // BƯỚC 2: Load User từ DB (Dùng hàm loadUserByGoogleEmail trong CustomUserDetailsService)
        // Nếu không tìm thấy user, hàm này sẽ tự throw Exception như bạn đã viết
        UserDetails userDetails = customUserDetailsService.loadUserByGoogleEmail(email);

        // BƯỚC 3: Tạo đối tượng Authentication (Set Context để Spring Security biết user đã login)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // BƯỚC 4: Sinh JWT của hệ thống (System Token)
        String token = jwtUtils.generateJwtToken(authentication);

        // BƯỚC 5: Trả về kết quả (Giống login thường)
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        List<String> roles = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return JwtResponse.builder()
                .token(token)
                .id(customUserDetails.getId())
                .username(customUserDetails.getUsername())
                .fullName(customUserDetails.getFullName())
                .ownerId(customUserDetails.getOwnerId())
                .roles(roles)
                .build();
    }
}