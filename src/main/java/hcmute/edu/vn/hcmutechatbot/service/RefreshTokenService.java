package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse; // Import DTO này
import hcmute.edu.vn.hcmutechatbot.model.RefreshToken;
import hcmute.edu.vn.hcmutechatbot.repository.RefreshTokenRepository;
import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Nên thêm Transactional

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.JWT_REFRESH_ExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * TẠO REFRESH TOKEN MỚI
     */
    public String createRefreshToken(String accountId, String username) {
        // 1. Sinh chuỗi token ngẫu nhiên
        String tokenString = jwtUtils.generateRefreshToken(username);

        // 2. Lưu xuống MongoDB
        RefreshToken refreshToken = RefreshToken.builder()
                .accountId(accountId)
                .token(tokenString)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenString;
    }

    /**
     * CẤP LẠI ACCESS TOKEN & REFRESH TOKEN MỚI (ROTATION)
     * Thay đổi kiểu trả về thành JwtResponse để chứa cả 2 token
     */
    @Transactional // Đảm bảo việc xóa cũ và thêm mới xảy ra cùng lúc, tránh lỗi data
    public JwtResponse generateNewAccessToken(String requestRefreshToken) {
        // BƯỚC 1: Validate chữ ký
        if (!jwtUtils.validateRefreshToken(requestRefreshToken)) {
            throw new RuntimeException("Refresh Token không hợp lệ (Validate failed)");
        }

        // BƯỚC 2: Tìm Token trong Database
        RefreshToken tokenInDB = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token không tồn tại trong hệ thống!"));

        // BƯỚC 3: Kiểm tra hạn sử dụng
        if (tokenInDB.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(tokenInDB);
            throw new RuntimeException("Refresh Token đã hết hạn. Vui lòng đăng nhập lại!");
        }

        // --- BẮT ĐẦU LOGIC XOAY VÒNG (ROTATION) ---

        // BƯỚC 4: Lấy thông tin user
        String username = jwtUtils.getUserNameFromRefreshToken(requestRefreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // BƯỚC 5: XÓA REFRESH TOKEN CŨ KHỎI DB (Ngăn chặn tái sử dụng)
        refreshTokenRepository.delete(tokenInDB);

        // BƯỚC 6: TẠO REFRESH TOKEN MỚI (Và lưu vào DB)
        String newRefreshToken = createRefreshToken(tokenInDB.getAccountId(), username);

        // BƯỚC 7: TẠO ACCESS TOKEN MỚI
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        String newAccessToken = jwtUtils.generateJwtToken(authentication);

        // BƯỚC 8: Trả về cả cặp token mới
        // (Sử dụng Builder của JwtResponse)
        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(username)
                .build();
    }

    // --- HÀM XÓA TOKEN KHI LOGOUT ---
    @Transactional
    public void deleteByToken(String token) {
        // Tìm token, nếu có thì xóa
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        refreshToken.ifPresent(refreshTokenRepository::delete);
    }
}