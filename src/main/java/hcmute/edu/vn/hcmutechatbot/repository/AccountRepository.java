package hcmute.edu.vn.hcmutechatbot.repository;

import hcmute.edu.vn.hcmutechatbot.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findAccountByUsername(String username);

    // Tìm user có personalEmail HOẶC schoolEmail trùng khớp
    @Query("{ '$or': [ { 'personalEmail': ?0 }, { 'schoolEmail': ?0 } ] }")
    Optional<Account> findAccountByEmail(String email);
}
