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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm Account
        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Logic lấy FullName dựa trên Roles và OwnerId
        String fullName = "Unknown User";

        if (account.getOwnerId() != null) {
            // Lấy danh sách roles của user
            var roles = account.getRoles();

            // CASE 1: Nếu là Sinh viên -> Tìm trong bảng Student
            if (roles.contains(Role.STUDENT)) {
                Student student = studentRepository.findById(account.getOwnerId()).orElse(null);
                if (student != null) {
                    fullName = student.getFullName();
                }
            }
            // CASE 2: Nếu là Giảng viên, Trưởng khoa, hoặc Quản lý -> Tìm trong bảng Lecturer
            // Lưu ý: Logic này giả định Manager/Faculty Head cũng nằm trong bảng Lecturer
            else if (roles.contains(Role.LECTURER) ||
                    roles.contains(Role.FACULTY_HEAD) ||
                    roles.contains(Role.MANAGER)) {

                Lecturer lecturer = lecturerRepository.findById(account.getOwnerId()).orElse(null);
                if (lecturer != null) {
                    fullName = lecturer.getFullName();
                }
            }
        }

        // 3. Build CustomUserDetails
        return CustomUserDetails.build(account, fullName);
    }
}
