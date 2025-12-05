package hcmute.edu.vn.hcmutechatbot.security.jwt;

import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.JWT_ACCESS_KEY}")
    private String accessKey;

    @Value("${app.JWT_ACCESS_ExpirationMs}")
    private int accessKeyExpirationMs;

    @Value("${app.JWT_REFRESH_KEY}")
    private String refreshKey;

    @Value("${app.JWT_REFRESH_ExpirationMs}")
    private int refreshKeyExpirationMs;

    // ================== KEY MANAGEMENT ==================

    // Key dành riêng cho Access Token
    private SecretKey getAccessTokenKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
    }

    // Key dành riêng cho Refresh Token (MỚI)
    private SecretKey getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
    }

    // ================== ACCESS TOKEN LOGIC ==================

    public String generateJwtToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("id", userPrincipal.getId())
                .claim("role", userPrincipal.getAuthorities().iterator().next().getAuthority())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + accessKeyExpirationMs))
                .signWith(getAccessTokenKey(), Jwts.SIG.HS256) // Dùng Access Key
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getAccessTokenKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        return validateToken(authToken, getAccessTokenKey());
    }

    // ================== REFRESH TOKEN LOGIC (MỚI) ==================

    /**
     * Tạo Refresh Token
     * Thường Refresh Token chỉ cần chứa Username (Subject) để định danh,
     * không cần chứa quá nhiều claim như role hay id vì nó chỉ dùng để xin cấp lại token.
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + refreshKeyExpirationMs))
                .signWith(getRefreshTokenKey(), Jwts.SIG.HS256) // Quan trọng: Dùng Refresh Key
                .compact();
    }

    /**
     * Lấy Username từ Refresh Token
     * Dùng để tìm user trong DB khi thực hiện renew token
     */
    public String getUserNameFromRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(getRefreshTokenKey()) // Verify bằng Refresh Key
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validate Refresh Token
     */
    public boolean validateRefreshToken(String authToken) {
        return validateToken(authToken, getRefreshTokenKey());
    }

    // ================== COMMON HELPERS ==================

    // Hàm validate chung để tránh viết lặp code try-catch
    private boolean validateToken(String authToken, SecretKey secretKey) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }
        return false;
    }
}