package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id;

    @Indexed(unique = true) // Đảm bảo token là duy nhất - UUID V4
    private String token;

    @DBRef // Liên kết sang bảng Account
    private Account account;

    @Indexed(expireAfter = "0s")
    private Instant expiryDate; // Thời gian hết hạn
}