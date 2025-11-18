package hcmute.edu.vn.hcmutechatbot.initializer;

import hcmute.edu.vn.hcmutechatbot.model.*;
import hcmute.edu.vn.hcmutechatbot.repository.SemesterSummaryRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentCredentialRepository;
import hcmute.edu.vn.hcmutechatbot.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final StudentCredentialRepository credentialRepository;
    private final SemesterSummaryRepository summaryRepository;
    // Bỏ qua ChatHistoryRepository

    private final PasswordEncoder passwordEncoder;

    // Constructor Injection
    @Autowired
    public DataInitializer(
            StudentRepository studentRepository,
            StudentCredentialRepository credentialRepository,
            SemesterSummaryRepository summaryRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.credentialRepository = credentialRepository;
        this.summaryRepository = summaryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Chỉ tạo dữ liệu nếu chưa tồn tại
        if (studentRepository.count() > 0) {
            System.out.println("--- DỮ LIỆU MẪU ĐÃ TỒN TẠI, BỎ QUA KHỞI TẠO ---");
            return;
        }

        System.out.println("\n--- BẮT ĐẦU KHỞI TẠO DỮ LIỆU MẪU ---");

        // -----------------------------------------------------
        // BƯỚC 1: TẠO VÀ LƯU STUDENT (Lấy ID bất biến)
        // -----------------------------------------------------

        // 1.1. Tạo các Sub-document cơ bản
        ContactInfo contactInfo = new ContactInfo(
                "Việt Nam", "TP. Hồ Chí Minh", "Linh Trung", "Phạm Văn Đồng",
                "0987654321", "20110301@hcmute.edu.vn", "sinhvien.test@gmail.com"
        );
        AcademicInfo academicInfo = new AcademicInfo(
                "Đại học", LocalDate.of(2020, 9, 15), "Chính quy",
                "Kỹ thuật", "Công nghệ Thông tin", "Khoa CNTT", "2020", "2024"
        );
        FamilyInfo familyInfo = new FamilyInfo("Nguyễn Văn A", "0901234567",
                "TP.HCM", "Nguyễn Văn B", "0901234568", "Trần Thị C", "0901234569"
        );

        // 1.2. Tạo Student và Lưu để lấy ID
        Student student = new Student(
                null, "20110301", "Nguyễn Văn Test", LocalDate.of(2002, 1, 1),
                "TP.HCM", "Nam", "079xxxxxxx", "Kinh", "Không",
                contactInfo, academicInfo, familyInfo
        );

        Student savedStudent = studentRepository.save(student);
        String studentId = savedStudent.getId();
        String studentCode = savedStudent.getStudentCode();

        System.out.println("✅ Đã tạo Student với MSSV: " + studentCode + " và ID: " + studentId);

        // -----------------------------------------------------
        // BƯỚC 2: TẠO VÀ LƯU STUDENT CREDENTIAL (Tham chiếu ID & Mật khẩu)
        // -----------------------------------------------------
        final String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        StudentCredential credential = new StudentCredential(
                null,
                studentId,
                contactInfo.getUniversityEmail(),
                contactInfo.getPersonalEmail(),
                encodedPassword,
                true
        );

        credentialRepository.save(credential);
        System.out.println("✅ Đã tạo StudentCredential cho MSSV " + studentCode);

        // -----------------------------------------------------
        // BƯỚC 3: TẠO VÀ LƯU SEMESTER SUMMARY (Dữ liệu phức tạp & nhúng)
        // -----------------------------------------------------

        // Tạo các thành phần nhúng
        Location location = new Location("E3-201", "E3", "Cơ sở 1");
        Subject subject1 = new Subject("Cơ sở dữ liệu", "Học về SQL", 3, SubjectType.BatBuoc);
        Subject subject2 = new Subject("Lập trình Java", "Học về OOP", 4, SubjectType.BatBuoc);

        Schedule schedule1 = new Schedule("Hai", "Tiết 1", "Tiết 3", "1-15", location);
        Schedule schedule2 = new Schedule("Ba", "Tiết 4", "Tiết 6", "1-15", location); // <--- Tạo thêm Schedule

        // Dùng SubjectType.Compulsory sau khi chuẩn hóa
        CourseSection courseSection1 = new CourseSection(
                subject1, "PGS.TS Nguyễn Văn D", 8.5, 7.5,
                "ThS. Trần Thị E", Set.of(schedule1)
        );
        CourseSection courseSection2 = new CourseSection(
                subject2, "TS. Lê Thị F", 9.0, 8.0,
                "ThS. Phan Hữu G", Set.of(schedule2)
        );

        Activity activity1 = new Activity(
                "Hội thảo khoa học", "Tham gia hội thảo về AI", 5,
                ActivityType.CTXH, "Phòng Đào tạo"
        );

        SemesterSummary summary = new SemesterSummary(
                null,
                studentId, // Tham chiếu
                "Học kỳ 1 năm 2024-2025", 7.0,
                Set.of(courseSection1, courseSection2),
                Set.of(activity1),
                LocalDate.of(2024, 9, 5),
                LocalDate.of(2025, 1, 15)
        );

        summaryRepository.save(summary);
        System.out.println("✅ Đã tạo SemesterSummary.");

        System.out.println("\n--- HOÀN TẤT KHỞI TẠO DỮ LIỆU MẪU ---");
        System.out.println("Tài khoản test (MSSV/Email): " + studentCode);
        System.out.println("Mật khẩu test: " + rawPassword);
    }
}
