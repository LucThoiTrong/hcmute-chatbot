package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Account;
import hcmute.edu.vn.hcmutechatbot.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);

    // Hàm này giúp xóa token cũ của user trước khi tạo mới
    void deleteByAccount(Account account);

    // Xóa tất cả token của 1 accountId cụ thể
    void deleteByAccountId(String accountId);
}