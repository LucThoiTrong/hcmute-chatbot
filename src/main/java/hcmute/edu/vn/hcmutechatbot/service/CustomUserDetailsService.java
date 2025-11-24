package hcmute.edu.vn.hcmutechatbot.service;

import hcmute.edu.vn.hcmutechatbot.model.Account;
import hcmute.edu.vn.hcmutechatbot.model.Lecturer;
import hcmute.edu.vn.hcmutechatbot.model.Student;
import hcmute.edu.vn.hcmutechatbot.model.enums.Role;
import hcmute.edu.vn.hcmutechatbot.repository.AccountRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm Account
        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Logic lấy FullName dựa trên Role và OwnerId
        String fullName = "Unknown User"; // Mặc định nếu không tìm thấy

        if (account.getOwnerId() != null) {
            if (account.getRole() == Role.STUDENT) {
                // Query bảng Student
                Student student = studentRepository.findById(account.getOwnerId()).orElse(null);
                if (student != null) {
                    fullName = student.getFullName();
                }
            } else if (account.getRole() == Role.LECTURER || account.getRole() == Role.MANAGER) {
                // Query bảng Lecturer
                Lecturer lecturer = lecturerRepository.findById(account.getOwnerId()).orElse(null);
                if (lecturer != null) {
                    fullName = lecturer.getFullName();
                }
            }
        }

        // 3. Build CustomUserDetails kèm theo fullName vừa tìm được
        return CustomUserDetails.build(account, fullName);
    }
}
