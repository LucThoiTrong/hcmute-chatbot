package hcmute.edu.vn.hcmutechatbot.config;

import hcmute.edu.vn.hcmutechatbot.model.*;
import hcmute.edu.vn.hcmutechatbot.model.enums.*;
import hcmute.edu.vn.hcmutechatbot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    // --- Repositories ---
    private final AccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final EducationProgramRepository programRepository;
    private final CourseClassRepository courseClassRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Chỉ khởi tạo khi DB trống (check bảng Account)
        if (accountRepository.count() == 0) {
            System.out.println("🚀 BẮT ĐẦU KHỞI TẠO DỮ LIỆU HCMUTE CHATBOT (FULL DATA)...");

            // 1. Khởi tạo KHOA - NGÀNH - LĨNH VỰC TƯ VẤN (Thỏa mãn: 2 Khoa, 1 Khoa 2 Ngành, 1 Khoa 3 Lĩnh vực)
            initFacultyAndDomains();

            // 2. Khởi tạo USERS (2 SV, 1 Manager, Nhiều GV)
            initUsersAndAccounts();

            // 3. Khởi tạo MÔN HỌC & CHƯƠNG TRÌNH ĐÀO TẠO
            initCoursesAndPrograms();

            // 4. Khởi tạo LỚP HỌC PHẦN & ĐĂNG KÝ
            initClassesAndEnrollments();

            // 5. Khởi tạo HỘI THOẠI MẪU
            initConversationsAndMessages();

            // 6. Khởi tạo THÔNG BÁO
            initNotifications();

            System.out.println("✅ KHỞI TẠO DỮ LIỆU HOÀN TẤT!");
        }
    }

    // ==========================================
    // 1. DATA KHOA & LĨNH VỰC TƯ VẤN
    // ==========================================
    private void initFacultyAndDomains() {
        // --- A. KHOA CNTT (Faculty IT) - Thỏa mãn nhiều điều kiện nhất ---

        // 1. Chuyên ngành & Ngành
        Specialization specSE = Specialization.builder().id("S_SE").name("Công nghệ phần mềm").description("Phát triển ứng dụng").build();
        Specialization specAI = Specialization.builder().id("S_AI").name("Trí tuệ nhân tạo").description("Deep Learning, ML").build();

        Major majorIT = Major.builder() // Ngành 1
                .id("M_IT").name("Công nghệ Thông tin").description("Đào tạo kỹ sư CNTT")
                .specializations(Set.of(specSE))
                .build();

        Major majorDS = Major.builder() // Ngành 2 (Thỏa mãn: 1 Khoa có 2 ngành)
                .id("M_DS").name("Kỹ thuật Dữ liệu").description("Data Science & Big Data")
                .specializations(Set.of(specAI))
                .build();

        // 2. Lĩnh vực tư vấn (Thỏa mãn: 1 Khoa có 3 lĩnh vực)
        // Domain 1: Học tập
        AdvisoryDomain domainAcademicIT = AdvisoryDomain.builder()
                .id("D_IT_ACADEMIC").name("Cố vấn học tập CNTT").description("Tư vấn lộ trình, đăng ký môn")
                .consultantIds(Set.of("GV_IT_01")) // Thầy A
                .build();

        // Domain 2: Nghiên cứu khoa học
        AdvisoryDomain domainResearchIT = AdvisoryDomain.builder()
                .id("D_IT_RESEARCH").name("Nghiên cứu khoa học").description("Hướng dẫn đề tài, viết báo")
                .consultantIds(Set.of("GV_IT_02")) // Cô B
                .build();

        // Domain 3: Thực tập & Việc làm
        AdvisoryDomain domainJobIT = AdvisoryDomain.builder()
                .id("D_IT_JOB").name("Thực tập & Việc làm").description("Giới thiệu công ty thực tập")
                .consultantIds(Set.of("GV_IT_03")) // Thầy C
                .build();

        Faculty facultyIT = Faculty.builder()
                .id("F_IT").name("Khoa Công nghệ Thông tin").description("Faculty of IT")
                .type(FacultyType.ACADEMIC)
                .majors(Set.of(majorIT, majorDS))
                .advisoryDomains(Set.of(domainAcademicIT, domainResearchIT, domainJobIT))
                .build();

        // --- B. KHOA KINH TẾ (Faculty Economics) - Khoa thứ 2 ---
        Major majorLogistics = Major.builder()
                .id("M_LOG").name("Logistics").description("Quản lý chuỗi cung ứng")
                .specializations(Collections.emptySet())
                .build();

        AdvisoryDomain domainAcademicEco = AdvisoryDomain.builder()
                .id("D_ECO_ACADEMIC").name("Cố vấn học tập Kinh tế").description("Tư vấn sinh viên kinh tế")
                .consultantIds(Set.of("GV_ECO_01"))
                .build();

        Faculty facultyEco = Faculty.builder()
                .id("F_ECO").name("Khoa Kinh tế").description("Faculty of Economics")
                .type(FacultyType.ACADEMIC)
                .majors(Set.of(majorLogistics))
                .advisoryDomains(Set.of(domainAcademicEco))
                .build();

        // --- C. PHÒNG CÔNG TÁC SINH VIÊN (Service Faculty) ---
        AdvisoryDomain domainStudentAffairs = AdvisoryDomain.builder()
                .id("D_CTSV").name("Công tác sinh viên").description("Học bổng, Rèn luyện, Ngoại trú")
                .consultantIds(Set.of("GV_ADMIN")) // Manager kiêm tư vấn
                .build();

        Faculty officeStudent = Faculty.builder()
                .id("F_SA").name("Phòng Công tác Sinh viên").description("Hỗ trợ đời sống sinh viên")
                .type(FacultyType.SERVICE)
                .majors(Collections.emptySet())
                .advisoryDomains(Set.of(domainStudentAffairs))
                .build();

        facultyRepository.saveAll(List.of(facultyIT, facultyEco, officeStudent));
        System.out.println("   -> Đã tạo: Khoa CNTT (2 ngành, 3 lĩnh vực), Khoa Kinh tế, Phòng CTSV");
    }

    // ==========================================
    // 2. DATA USERS & ACCOUNTS
    // ==========================================
    private void initUsersAndAccounts() {
        List<Lecturer> lecturers = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        String defaultPass = passwordEncoder.encode("123456");

        // --- 2.1 GIẢNG VIÊN (Khớp với ID trong AdvisoryDomain ở trên) ---
        // GV CNTT
        lecturers.add(Lecturer.builder().id("GV_IT_01").fullName("TS. Nguyễn Văn Code").facultyId("F_IT").facultyName("Khoa CNTT").build());
        lecturers.add(Lecturer.builder().id("GV_IT_02").fullName("PGS. Trần Thị Data").facultyId("F_IT").facultyName("Khoa CNTT").build());
        lecturers.add(Lecturer.builder().id("GV_IT_03").fullName("ThS. Lê Văn Job").facultyId("F_IT").facultyName("Khoa CNTT").build());
        // GV Kinh Tế
        lecturers.add(Lecturer.builder().id("GV_ECO_01").fullName("TS. Phạm Kinh Tế").facultyId("F_ECO").facultyName("Khoa Kinh tế").build());
        // Admin / Manager
        lecturers.add(Lecturer.builder().id("GV_ADMIN").fullName("Thầy Trưởng Phòng").facultyId("F_SA").facultyName("Phòng CTSV").build());

        lecturerRepository.saveAll(lecturers);

        // Tạo Account cho GV
        for (Lecturer lec : lecturers) {
            Role role = lec.getId().equals("GV_ADMIN") ? Role.MANAGER : Role.LECTURER;
            accounts.add(Account.builder()
                    .username(lec.getId().toLowerCase())
                    .password(defaultPass)
                    .role(role)
                    .ownerId(lec.getId())
                    .personalEmail(lec.getId().toLowerCase() + "@hcmute.edu.vn")
                    .build());
        }

        // --- 2.2 SINH VIÊN (Thỏa mãn: Ít nhất 2 SV) ---

        // SV 1: Khoa CNTT - Năm 3
        Student s1 = Student.builder()
                .studentId("22110254").fullName("Lục Thới Trọng").birthDate(LocalDate.of(2003, 5, 20))
                .gender(Gender.MALE).citizenId("079000000001")
                .contactInfo(ContactInfo.builder().mobilePhone("0901234567").personalEmail("sv_it@gmail.com").build())
                .academicInfo(AcademicInfo.builder()
                        .cohort("2021").admissionDate(LocalDate.of(2021, 9, 5))
                        .facultyId("F_IT").facultyName("Khoa CNTT").majorId("M_IT").majorName("Công nghệ Thông tin")
                        .build())
                .build();
        students.add(s1);

        // SV 2: Khoa Kinh Tế - Năm 1
        Student s2 = Student.builder()
                .studentId("22110177").fullName("Bùi Đức Lộc").birthDate(LocalDate.of(2005, 8, 15))
                .gender(Gender.MALE).citizenId("079000000002")
                .contactInfo(ContactInfo.builder().mobilePhone("0909876543").personalEmail("sv_eco@gmail.com").build())
                .academicInfo(AcademicInfo.builder()
                        .cohort("2023").admissionDate(LocalDate.of(2023, 9, 5))
                        .facultyId("F_ECO").facultyName("Khoa Kinh tế").majorId("M_LOG").majorName("Logistics")
                        .build())
                .build();
        students.add(s2);

        studentRepository.saveAll(students);

        // Tạo Account cho SV
        for (Student stu : students) {
            accounts.add(Account.builder()
                    .username(stu.getStudentId())
                    .password(defaultPass)
                    .role(Role.STUDENT)
                    .ownerId(stu.getStudentId())
                    .personalEmail(stu.getContactInfo().getPersonalEmail())
                    .build());
        }

        accountRepository.saveAll(accounts);
        System.out.println("   -> Đã tạo: 5 Lecturers, 2 Students, 7 Accounts (bao gồm 1 Manager)");
    }

    // ==========================================
    // 3. DATA MÔN HỌC & CHƯƠNG TRÌNH
    // ==========================================
    private void initCoursesAndPrograms() {
        // Môn CNTT
        Course cJava = Course.builder().id("INT101").name("Lập trình Java").facultyId("F_IT").lecturers(Set.of("GV_IT_01")).build();
        Course cData = Course.builder().id("INT102").name("Cấu trúc dữ liệu").facultyId("F_IT").lecturers(Set.of("GV_IT_01")).build();
        // Môn Kinh Tế
        Course cMacro = Course.builder().id("ECO101").name("Kinh tế vĩ mô").facultyId("F_ECO").lecturers(Set.of("GV_ECO_01")).build();

        courseRepository.saveAll(List.of(cJava, cData, cMacro));

        // Program CNTT
        ProgramSubject subJava = ProgramSubject.builder().courseId("INT101").courseName("Lập trình Java").subjectType(SubjectType.COMPULSORY).credits(3).semester(1).build();
        EducationProgram programIT = EducationProgram.builder()
                .majorId("M_IT").cohort("2021")
                .subjects(Set.of(subJava))
                .build();

        // Program Kinh Tế
        ProgramSubject subMacro = ProgramSubject.builder().courseId("ECO101").courseName("Kinh tế vĩ mô").subjectType(SubjectType.COMPULSORY).credits(3).semester(1).build();
        EducationProgram programEco = EducationProgram.builder()
                .majorId("M_LOG").cohort("2023")
                .subjects(Set.of(subMacro))
                .build();

        programRepository.saveAll(List.of(programIT, programEco));
        System.out.println("   -> Đã tạo: Môn học & CTĐT cho 2 khoa");
    }

    // ==========================================
    // 4. DATA LỚP HỌC & ENROLLMENT
    // ==========================================
    private void initClassesAndEnrollments() {
        // Lớp Java cho SV IT
        TimeSlot slot1 = TimeSlot.builder().dayOfWeek("MONDAY").startPeriod(1).endPeriod(3).room("A1-201").campus("Cơ sở 1").build();
        CourseClass classJava = CourseClass.builder()
                .id("CL_JAVA_01").name("Java - Nhóm 01")
                .courseId("INT101").courseName("Lập trình Java")
                .lecturerId("GV_IT_01").lecturerName("TS. Nguyễn Văn Code")
                .semester("HK1_2023_2024").academicYear("2023-2024")
                .studentIds(Set.of("21110001"))
                .timeSlots(Set.of(slot1))
                .build();
        courseClassRepository.save(classJava);

        // Điểm SV IT
        StudentEnrollment enroll1 = StudentEnrollment.builder()
                .studentId("21110001").courseClassId("CL_JAVA_01")
                .semester("HK1_2023_2024").academicYear("2023-2024")
                .midtermScore(8.0).finalScore(9.0).totalScore(8.5)
                .build();
        enrollmentRepository.save(enroll1);
        System.out.println("   -> Đã tạo: Lớp học & Điểm số");
    }

    // ==========================================
    // 5. DATA CHAT & TIN NHẮN
    // ==========================================
    private void initConversationsAndMessages() {
        LocalDateTime now = LocalDateTime.now();

        // Chat 1: SV IT hỏi về NCKH (Advisory - Private)
        Conversation convResearch = Conversation.builder()
                .title("Tư vấn tham gia NCKH")
                .type(ConversationType.ADVISORY)
                .mode(ConversationMode.PRIVATE)
                .status(ConversationStatus.OPEN)
                .createdAt(now)
                .createdByUserId("21110001")
                .facultyId("F_IT").facultyName("Khoa CNTT")
                .advisoryDomainId("D_IT_RESEARCH").advisoryDomainName("Nghiên cứu khoa học")
                .participantIds(Set.of("21110001", "GV_IT_02")) // SV IT & Cô Data
                .build();
        conversationRepository.save(convResearch);

        Message msg1 = Message.builder().conversationId(convResearch.getId()).content("Em muốn tham gia nhóm NCKH về AI ạ.").senderId("21110001").senderType(SenderType.USER).sentAt(now).build();
        messageRepository.save(msg1);

        System.out.println("   -> Đã tạo: Chat tư vấn NCKH");
    }

    // ==========================================
    // 6. DATA THÔNG BÁO
    // ==========================================
    private void initNotifications() {
        Notification noti = Notification.builder()
                .title("Thông báo nộp học phí HK2").content("Hạn chót 15/01/2024.")
                .senderId("GV_ADMIN")
                .scope(NotificationScope.GLOBAL)
                .timestamp(LocalDateTime.now())
                .build();
        notificationRepository.save(noti);
        System.out.println("   -> Đã tạo: Thông báo chung");
    }
}