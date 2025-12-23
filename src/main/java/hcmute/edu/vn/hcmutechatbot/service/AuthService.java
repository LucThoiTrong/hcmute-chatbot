package hcmute.edu.vn.hcmutechatbot.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse;
import hcmute.edu.vn.hcmutechatbot.exception.InvalidTokenException;
import hcmute.edu.vn.hcmutechatbot.exception.ResourceNotFoundException;
import hcmute.edu.vn.hcmutechatbot.mapper.AuthMapper;
import hcmute.edu.vn.hcmutechatbot.model.Account;
import hcmute.edu.vn.hcmutechatbot.model.PasswordResetToken;
import hcmute.edu.vn.hcmutechatbot.repository.AccountRepository;
import hcmute.edu.vn.hcmutechatbot.repository.PasswordResetTokenRepository;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final GoogleAuthService googleAuthService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;
    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public JwtResponse loginWithGoogle(String googleToken) {
        // B1: Verify Token
        GoogleIdToken.Payload payload = googleAuthService.verifyToken(googleToken);
        if (payload == null) {
            throw new InvalidTokenException("Token Google không hợp lệ hoặc đã hết hạn!");
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

        // B5: Sinh & Lưu Refresh Token
        String refreshToken = refreshTokenService.createRefreshToken(customUserDetails.getId(), customUserDetails.getUsername());

        // B6: Dùng Mapper để trả về kết quả đồng bộ với Login thường
        return authMapper.toJwtResponse(accessToken, refreshToken, customUserDetails);
    }

    // LOGIC 1: XỬ LÝ YÊU CẦU QUÊN MẬT KHẨU ---
    public void processForgotPassword(String email) {
        log.info("Processing forgot password for email: {}", email);
        // 1. Tìm account (Dùng hàm tìm cả email cá nhân và trường học)
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại trong hệ thống!"));

        // 2. Xóa token cũ nếu có (Tránh rác DB)
        passwordResetTokenRepository.deleteByAccount(account);

        // 3. Tạo token ngẫu nhiên (UUID)
        String tokenString = UUID.randomUUID().toString();

        // 4. Lưu token vào DB (Hết hạn sau 10 phút)
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenString)
                .account(account)
                .expiryDate(Instant.now().plusSeconds(600)) // 600s = 10 phút
                .build();

        passwordResetTokenRepository.save(resetToken);

        // 5. Gửi mail
//        String link = "http://localhost:5173/reset-password?token=" + tokenString;
        String link = "https://hcmuteassistant.dpdns.org/reset-password?token=" + tokenString;
        // GỬI MAIL THẬT
        emailService.sendResetPasswordEmail(email, link);
    }

    // --- LOGIC 2: XỬ LÝ ĐẶT LẠI MẬT KHẨU ---
    public void processResetPassword(String token, String newPassword) {
        // 1. Tìm token trong DB
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Link không hợp lệ hoặc không tồn tại!"));

        // 2. Kiểm tra hết hạn
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken); // Xóa token hết hạn
            throw new InvalidTokenException("Link đã hết hạn. Vui lòng gửi lại yêu cầu!");
        }

        // 3. Cập nhật mật khẩu mới cho Account
        Account account = resetToken.getAccount();
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        // 4. Xóa token reset vừa dùng (Chỉ dùng 1 lần)
        passwordResetTokenRepository.delete(resetToken);

        // (Optional) Nếu muốn bảo mật hơn, bạn có thể xóa hết RefreshToken của user này
        refreshTokenService.deleteByAccountId(account.getId());
    }
}