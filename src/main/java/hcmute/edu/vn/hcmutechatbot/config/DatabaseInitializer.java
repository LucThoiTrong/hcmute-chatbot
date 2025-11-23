package hcmute.edu.vn.hcmutechatbot.config;

import hcmute.edu.vn.hcmutechatbot.model.Account;
import hcmute.edu.vn.hcmutechatbot.model.enums.Role;
import hcmute.edu.vn.hcmutechatbot.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (accountRepository.count() == 0) {
            Account admin = Account.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.MANAGER)
                    .personalEmail("admin@hcmute.edu.vn")
                    .isActive(true)
                    .ownerId("ADMIN_001")
                    .build();

            accountRepository.save(admin);
            System.out.println("✅ Đã khởi tạo tài khoản mẫu: admin / 123456");
        }
    }
}
