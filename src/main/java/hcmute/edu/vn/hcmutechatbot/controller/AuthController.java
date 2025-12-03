package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.GoogleLoginRequest; // Import DTO mới
import hcmute.edu.vn.hcmutechatbot.dto.request.LoginRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import hcmute.edu.vn.hcmutechatbot.service.AuthService; // Import AuthService
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // [NEW] Inject AuthService để xử lý logic Google
    private final AuthService authService;

    // API Đăng nhập thường (Giữ nguyên code cũ của bạn)
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(JwtResponse.builder()
                .token(token)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .ownerId(userDetails.getOwnerId())
                .roles(roles)
                .build()
        );
    }

    // [NEW] API Đăng nhập bằng Google
    // Endpoint: POST /api/auth/google
    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody GoogleLoginRequest request) {
        try {
            // Gọi xuống Service để Verify token và lấy JWT hệ thống
            JwtResponse jwtResponse = authService.loginWithGoogle(request.getToken());

            // Trả về kết quả thành công (200 OK)
            return ResponseEntity.ok(jwtResponse);

        } catch (RuntimeException e) {
            // Xử lý lỗi theo mô tả trong ảnh (Case Not Found hoặc Token lỗi)
            // Trả về HTTP 401 Unauthorized kèm thông báo lỗi
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}