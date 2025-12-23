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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AccountRepository accountRepository;

    // --- 1. ĐĂNG NHẬP BẰNG USERNAME (Login thường) ---
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tên đăng nhập không tồn tại trong hệ thống"));

        return buildUserDetails(account);
    }

    // --- 2. ĐĂNG NHẬP BẰNG GOOGLE EMAIL (Login Google) ---
    public UserDetails loadUserByGoogleEmail(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản Google chưa được đăng ký trong hệ thống"));

        return buildUserDetails(account);
    }

    // --- 3. CORE LOGIC (Dùng chung) ---
    private CustomUserDetails buildUserDetails(Account account) {
        // 1. Lấy FullName
        String fullName = getUserFullName(account);

        // 2. Lấy FacultyId theo logic từng Role
        String facultyId = getUserFacultyId(account);

        return CustomUserDetails.build(account, fullName, facultyId);
    }

    private String getUserFacultyId(Account account) {
        if (account.getOwnerId() == null) return "ALL";

        Set<Role> roles = account.getRoles();

        // CASE 1: Sinh viên -> Lấy trong academicInfo
        if (roles.contains(Role.STUDENT)) {
            return studentRepository.findById(account.getOwnerId())
                    .map(student -> {
                        if (student.getAcademicInfo() != null) {
                            return student.getAcademicInfo().getFacultyId();
                        }
                        return "ALL";
                    })
                    .orElse("ALL");
        }

        // CASE 2: Giảng viên
        if (roles.contains(Role.LECTURER) ||
                roles.contains(Role.FACULTY_HEAD)) {

            return lecturerRepository.findById(account.getOwnerId())
                    .map(Lecturer::getFacultyId)
                    .orElse("ALL");
        }

        return "ALL";
    }

    // Logic nghiệp vụ xác định tên người dùng dựa trên Role
    private String getUserFullName(Account account) {
        if (account.getOwnerId() == null) {
            return "Unknown User";
        }

        Set<Role> roles = account.getRoles();

        // CASE 1: Sinh viên
        if (roles.contains(Role.STUDENT)) {
            return studentRepository.findById(account.getOwnerId())
                    .map(Student::getFullName)
                    .orElse("Unknown Student");
        }

        // CASE 2: Giảng viên
        if (roles.contains(Role.LECTURER) || roles.contains(Role.FACULTY_HEAD)) {
            return lecturerRepository.findById(account.getOwnerId())
                    .map(Lecturer::getFullName)
                    .orElse("Unknown Lecturer");
        }

        return "Unknown User";
    }
}