package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.ForgotPasswordRequest;
import hcmute.edu.vn.hcmutechatbot.dto.request.GoogleLoginRequest;
import hcmute.edu.vn.hcmutechatbot.dto.request.LoginRequest;
import hcmute.edu.vn.hcmutechatbot.dto.request.ResetPasswordRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse;
import hcmute.edu.vn.hcmutechatbot.mapper.AuthMapper;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import hcmute.edu.vn.hcmutechatbot.service.AuthService;
import hcmute.edu.vn.hcmutechatbot.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    // --- 1. LOGIN THƯỜNG ---
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getId(), userDetails.getUsername());

        ResponseCookie cookie = generateRefreshTokenCookie(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authMapper.toJwtResponse(accessToken, null, userDetails));
    }

    // --- 2. LOGIN GOOGLE ---
    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody GoogleLoginRequest request) {
        try {
            JwtResponse jwtResponse = authService.loginWithGoogle(request.getToken());
            String refreshToken = jwtResponse.getRefreshToken();

            ResponseCookie cookie = generateRefreshTokenCookie(refreshToken);
            jwtResponse.setRefreshToken(null);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(jwtResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // --- 2.1 QUÊN MẬT KHẨU (GỬI EMAIL) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            // Gọi service để tìm user, tạo token và gửi email
            authService.processForgotPassword(request.getEmail());
            return ResponseEntity.ok("Link đặt lại mật khẩu đã được gửi vào email của bạn (nếu tồn tại)!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 2.2 ĐẶT LẠI MẬT KHẨU ---
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // Gọi service để verify token và update mật khẩu mới
            authService.processResetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 3. REFRESH TOKEN (CẬP NHẬT LOGIC ROTATION) ---
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshtoken(@CookieValue(name = "refreshToken", required = false) String oldRefreshToken) {
        // 1. Kiểm tra Cookie có tồn tại không
        if (oldRefreshToken == null || oldRefreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Refresh Token is empty in Cookie! Please login again.");
        }

        try {
            // 2. Gọi Service để thực hiện xoay vòng token (Xóa cũ - Tạo mới)
            // Service trả về một cặp token mới toanh
            JwtResponse tokenPair = refreshTokenService.generateNewAccessToken(oldRefreshToken);

            String newAccessToken = tokenPair.getAccessToken();
            String newRefreshToken = tokenPair.getRefreshToken();

            // 3. Tạo Cookie mới chứa Refresh Token MỚI
            ResponseCookie newCookie = generateRefreshTokenCookie(newRefreshToken);

            // 4. Trả về Response
            // - Header: Set-Cookie (ghi đè cookie cũ bằng cái mới)
            // - Body: Chỉ chứa Access Token mới (ẩn refresh token đi)
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                    .body(authMapper.toJwtResponse(newAccessToken, null));

        } catch (RuntimeException e) {
            // Nếu lỗi (ví dụ token cũ không tìm thấy do đã bị xóa -> dấu hiệu bị hack hoặc lỗi mạng)
            // Trả về 403 Forbidden để Client biết đường logout
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // --- 4. LOGOUT ---
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body("You've been signed out!");
    }

    // === HÀM PRIVATE HỖ TRỢ ===
    private ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }
}