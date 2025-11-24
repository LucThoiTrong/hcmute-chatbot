package hcmute.edu.vn.hcmutechatbot.config;

import hcmute.edu.vn.hcmutechatbot.model.*;
import hcmute.edu.vn.hcmutechatbot.model.enums.Gender;
import hcmute.edu.vn.hcmutechatbot.model.enums.Role;
import hcmute.edu.vn.hcmutechatbot.repository.AccountRepository;
import hcmute.edu.vn.hcmutechatbot.repository.LecturerRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (accountRepository.count() == 0) {
            System.out.println("🚀 Đang khởi tạo dữ liệu mẫu...");

            // ==========================================
            // 1. TẠO DATA GIẢNG VIÊN (3 NGƯỜI)
            // ==========================================

            // Giảng viên 1 (Role LECTURER)
            Lecturer lecturer1 = Lecturer.builder()
                    .id("GV001")
                    .fullName("ThS. Nguyễn Văn A")
                    .facultyId("F_IT")
                    .facultyName("Khoa Công nghệ Thông tin")
                    .build();

            // Giảng viên 2 (Role LECTURER)
            Lecturer lecturer2 = Lecturer.builder()
                    .id("GV002")
                    .fullName("TS. Trần Thị B")
                    .facultyId("F_EE")
                    .facultyName("Khoa Điện - Điện tử")
                    .build();

            // Giảng viên 3 -> Người này sẽ giữ Role MANAGER
            Lecturer managerInfo = Lecturer.builder()
                    .id("GV_ADMIN")
                    .fullName("PGS.TS. Lê Văn Sếp (Trưởng Khoa)")
                    .facultyId("F_IT")
                    .facultyName("Khoa Công nghệ Thông tin")
                    .build();

            lecturerRepository.saveAll(List.of(lecturer1, lecturer2, managerInfo));

            // ==========================================
            // 2. TẠO DATA SINH VIÊN (1 NGƯỜI)
            // ==========================================

            // Data nhúng (Embedded)
            ContactInfo contactInfo = ContactInfo.builder()
                    .country("Việt Nam").province("TP.HCM").ward("Linh Chiểu").streetAddress("01 Võ Văn Ngân")
                    .mobilePhone("0987654321").personalEmail("sv.c@gmail.com").schoolEmail("20110001@student.hcmute.edu.vn")
                    .build();

            AcademicInfo academicInfo = AcademicInfo.builder()
                    .cohort("2020").admissionDate(LocalDate.of(2020, 9, 5))
                    .trainingType("Đại trà").trainingProgram("Kỹ sư").academicYear("2020-2024")
                    .facultyId("F_IT").facultyName("CNTT").majorId("M_SE").majorName("KTPM").specializationName("CNPM")
                    .build();

            // Chỉ cần tạo object đơn giản cho ContactPerson để code gọn
            ContactPerson contactPerson = new ContactPerson();

            Student student = Student.builder()
                    .studentId("20110001")
                    .fullName("Nguyễn Văn C")
                    .birthDate(LocalDate.of(2002, 1, 1))
                    .gender(Gender.MALE) // Đảm bảo Enum Gender khớp
                    .citizenId("012345678900")
                    .contactInfo(contactInfo)
                    .academicInfo(academicInfo)
                    .contactPerson(contactPerson)
                    .build();

            studentRepository.save(student);

            // ==========================================
            // 3. TẠO TÀI KHOẢN (ACCOUNTS)
            // ==========================================

            // --- ACCOUNT 1: MANAGER (Link tới GV_ADMIN) ---
            Account managerAcc = Account.builder()
                    .username("manager") // Username đăng nhập
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.MANAGER) // Quyền cao nhất
                    .ownerId("GV_ADMIN") // Trỏ về bản ghi của Sếp trong bảng lecturers
                    .personalEmail("sep.le@hcmute.edu.vn")
                    .isActive(true)
                    .build();

            // --- ACCOUNT 2: LECTURER 1 (Link tới GV001) ---
            Account lecAcc1 = Account.builder()
                    .username("gv001")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.LECTURER)
                    .ownerId("GV001")
                    .personalEmail("nguyenvana@hcmute.edu.vn")
                    .isActive(true)
                    .build();

            // --- ACCOUNT 3: LECTURER 2 (Link tới GV002) ---
            Account lecAcc2 = Account.builder()
                    .username("gv002")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.LECTURER)
                    .ownerId("GV002")
                    .personalEmail("tranthib@hcmute.edu.vn")
                    .isActive(true)
                    .build();

            // --- ACCOUNT 4: STUDENT (Link tới 20110001) ---
            Account stuAcc = Account.builder()
                    .username("20110001")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.STUDENT)
                    .ownerId("20110001")
                    .personalEmail("sv.c@gmail.com")
                    .isActive(true)
                    .build();

            // Lưu tất cả Account
            accountRepository.saveAll(List.of(managerAcc, lecAcc1, lecAcc2, stuAcc));

            System.out.println("✅ Đã khởi tạo thành công:");
            System.out.println("   -------------------------------------------------");
            System.out.println("   | User     | Pass   | Role     | Info Link      |");
            System.out.println("   -------------------------------------------------");
            System.out.println("   | manager  | 123456 | MANAGER  | -> GV_ADMIN    |");
            System.out.println("   | gv001    | 123456 | LECTURER | -> GV001       |");
            System.out.println("   | gv002    | 123456 | LECTURER | -> GV002       |");
            System.out.println("   | 20110001 | 123456 | STUDENT  | -> 20110001    |");
            System.out.println("   -------------------------------------------------");
        }
    }
}