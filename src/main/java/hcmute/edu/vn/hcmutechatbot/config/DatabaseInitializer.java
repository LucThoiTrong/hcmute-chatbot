package hcmute.edu.vn.hcmutechatbot.config;

import hcmute.edu.vn.hcmutechatbot.model.*;
import hcmute.edu.vn.hcmutechatbot.model.enums.*;
import hcmute.edu.vn.hcmutechatbot.repository.*;
import hcmute.edu.vn.hcmutechatbot.util.TimeUtils; // <--- Import quan trọng
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
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

    private final PasswordEncoder passwordEncoder;

    // --- CONSTANTS CHO DATA MẪU ---
    private static final String STUDENT_IT_ID = "22110254";
    private static final String STUDENT_ECO_ID = "22110177";

    @Override
    public void run(String... args) {
        // Chỉ khởi tạo khi DB trống
        if (accountRepository.count() == 0) {
            System.out.println("🚀 BẮT ĐẦU KHỞI TẠO DỮ LIỆU HCMUTE CHATBOT (FULL REAL DATA)...");

            initFacultyAndDomains();
            initUsersAndAccounts();
            initCoursesAndPrograms();
            initClassesAndEnrollments();

            System.out.println("✅ KHỞI TẠO DỮ LIỆU HOÀN TẤT!");
            log.info("✅ KHỞI TẠO DỮ LIỆU HOÀN TẤT!");
        }
    }

    // ==========================================
    // 1. DATA KHOA & LĨNH VỰC TƯ VẤN
    // ==========================================
    private void initFacultyAndDomains() {
        // --- KHOA CNTT ---
        Specialization specSE = Specialization.builder().id("S_SE").name("Công nghệ phần mềm").description("Phát triển ứng dụng").build();
        Specialization specAI = Specialization.builder().id("S_AI").name("Trí tuệ nhân tạo").description("Deep Learning, ML").build();

        Major majorIT = Major.builder().id("M_IT").name("Công nghệ Thông tin").description("Đào tạo kỹ sư CNTT").specializations(Set.of(specSE)).build();
        Major majorDS = Major.builder().id("M_DS").name("Kỹ thuật Dữ liệu").description("Data Science").specializations(Set.of(specAI)).build();

        AdvisoryDomain domainAcademicIT = AdvisoryDomain.builder().id("D_IT_ACADEMIC").name("Cố vấn học tập CNTT").description("Tư vấn lộ trình").consultantIds(Set.of("GV_IT_01")).build();
        AdvisoryDomain domainResearchIT = AdvisoryDomain.builder().id("D_IT_RESEARCH").name("Nghiên cứu khoa học").description("Hướng dẫn đề tài").consultantIds(Set.of("GV_IT_02")).build();
        AdvisoryDomain domainJobIT = AdvisoryDomain.builder().id("D_IT_JOB").name("Thực tập & Việc làm").description("Giới thiệu việc làm").consultantIds(Set.of("GV_IT_03")).build();

        Faculty facultyIT = Faculty.builder().id("F_IT").name("Khoa Công nghệ Thông tin").type(FacultyType.ACADEMIC).majors(Set.of(majorIT, majorDS)).advisoryDomains(Set.of(domainAcademicIT, domainResearchIT, domainJobIT)).build();

        // --- KHOA KINH TẾ ---
        Major majorLogistics = Major.builder().id("M_LOG").name("Logistics").description("Chuỗi cung ứng").specializations(Collections.emptySet()).build();
        AdvisoryDomain domainAcademicEco = AdvisoryDomain.builder().id("D_ECO_ACADEMIC").name("Cố vấn học tập Kinh tế").description("Tư vấn kinh tế").consultantIds(Set.of("GV_ECO_01")).build();
        Faculty facultyEco = Faculty.builder().id("F_ECO").name("Khoa Kinh tế").type(FacultyType.ACADEMIC).majors(Set.of(majorLogistics)).advisoryDomains(Set.of(domainAcademicEco)).build();

        // --- PHÒNG CTSV ---
        AdvisoryDomain domainStudentAffairs = AdvisoryDomain.builder().id("D_CTSV").name("Công tác sinh viên").description("Học bổng, Rèn luyện").consultantIds(Set.of("GV_ADMIN")).build();
        Faculty officeStudent = Faculty.builder().id("F_SA").name("Phòng Công tác Sinh viên").type(FacultyType.SERVICE).majors(Collections.emptySet()).advisoryDomains(Set.of(domainStudentAffairs)).build();

        facultyRepository.saveAll(List.of(facultyIT, facultyEco, officeStudent));
    }

    // ==========================================
    // 2. DATA USERS & ACCOUNTS
    // ==========================================
    private void initUsersAndAccounts() {
        lecturerRepository.deleteAll();
        studentRepository.deleteAll();
        accountRepository.deleteAll();

        List<Lecturer> lecturers = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        String defaultPass = passwordEncoder.encode("123456");

        // --- GIẢNG VIÊN (Define tên cụ thể ở đây) ---
        lecturers.add(Lecturer.builder().id("GV_IT_01").fullName("TS. Nguyễn Văn Code").facultyId("F_IT").facultyName("Khoa CNTT").build());
        lecturers.add(Lecturer.builder().id("GV_IT_02").fullName("PGS. Trần Thị Data").facultyId("F_IT").facultyName("Khoa CNTT").build());
        lecturers.add(Lecturer.builder().id("GV_IT_03").fullName("ThS. Lê Văn Job").facultyId("F_IT").facultyName("Khoa CNTT").build());
        lecturers.add(Lecturer.builder().id("GV_ECO_01").fullName("TS. Phạm Kinh Tế").facultyId("F_ECO").facultyName("Khoa Kinh tế").build());
        lecturers.add(Lecturer.builder().id("GV_ADMIN").fullName("Thầy Quản Lý").facultyId("F_SA").facultyName("Phòng CTSV").build());

        lecturerRepository.saveAll(lecturers);

        for (Lecturer lec : lecturers) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.LECTURER);
            if (lec.getId().equals("GV_IT_01")) roles.add(Role.FACULTY_HEAD);

            accounts.add(Account.builder().username(lec.getId()).password(defaultPass).roles(roles).ownerId(lec.getId()).personalEmail(lec.getId().toLowerCase() + "@hcmute.edu.vn").build());
        }

        // --- SINH VIÊN ---
        Student s1 = Student.builder().studentId(STUDENT_IT_ID).fullName("Lục Thới Trọng").birthDate(LocalDate.of(2003, 5, 20)).gender(Gender.MALE).citizenId("079000000001")
                .contactInfo(ContactInfo.builder().mobilePhone("0901234567").personalEmail("lucthoitrong@gmail.com").build())
                .academicInfo(AcademicInfo.builder().cohort("2021").admissionDate(LocalDate.of(2021, 9, 5)).facultyId("F_IT").facultyName("Khoa CNTT").majorId("M_IT").majorName("Công nghệ Thông tin").build()).build();
        students.add(s1);

        Student s2 = Student.builder().studentId(STUDENT_ECO_ID).fullName("Bùi Đức Lộc").birthDate(LocalDate.of(2005, 8, 15)).gender(Gender.MALE).citizenId("079000000002")
                .contactInfo(ContactInfo.builder().mobilePhone("0909876543").personalEmail("bigbossteamute@gmail.com").build())
                .academicInfo(AcademicInfo.builder().cohort("2023").admissionDate(LocalDate.of(2023, 9, 5)).facultyId("F_ECO").facultyName("Khoa Kinh tế").majorId("M_LOG").majorName("Logistics").build()).build();
        students.add(s2);

        studentRepository.saveAll(students);

        for (Student stu : students) {
            accounts.add(Account.builder().username(stu.getStudentId()).password(defaultPass).roles(Collections.singleton(Role.STUDENT)).ownerId(stu.getStudentId()).personalEmail(stu.getContactInfo().getPersonalEmail()).build());
        }

        accountRepository.saveAll(accounts);
    }

    // ==========================================
    // 3. DATA MÔN HỌC & CHƯƠNG TRÌNH
    // ==========================================
    private void initCoursesAndPrograms() {
        List<Course> courses = new ArrayList<>();
        // CNTT: Gán đúng GV phụ trách
        courses.add(Course.builder().id("INT101").name("Lập trình Java").facultyId("F_IT").lecturers(Set.of("GV_IT_01")).build());
        courses.add(Course.builder().id("INT102").name("Cấu trúc dữ liệu").facultyId("F_IT").lecturers(Set.of("GV_IT_01")).build());
        courses.add(Course.builder().id("INT103").name("Lập trình Web").facultyId("F_IT").lecturers(Set.of("GV_IT_03")).build());
        courses.add(Course.builder().id("INT104").name("Cơ sở dữ liệu").facultyId("F_IT").lecturers(Set.of("GV_IT_02")).build());
        courses.add(Course.builder().id("INT105").name("Trí tuệ nhân tạo").facultyId("F_IT").lecturers(Set.of("GV_IT_02")).build());
        courses.add(Course.builder().id("INT106").name("Khóa luận tốt nghiệp").facultyId("F_IT").lecturers(Set.of("GV_IT_01")).build());

        // Kinh Tế
        courses.add(Course.builder().id("ECO101").name("Kinh tế vĩ mô").facultyId("F_ECO").lecturers(Set.of("GV_ECO_01")).build());
        courses.add(Course.builder().id("ECO102").name("Kinh tế vi mô").facultyId("F_ECO").lecturers(Set.of("GV_ECO_01")).build());
        courses.add(Course.builder().id("ECO103").name("Marketing căn bản").facultyId("F_ECO").lecturers(Set.of("GV_ECO_01")).build());
        courses.add(Course.builder().id("ECO104").name("Quản trị Logistics").facultyId("F_ECO").lecturers(Set.of("GV_ECO_01")).build());
        courses.add(Course.builder().id("ECO105").name("Luật kinh doanh").facultyId("F_ECO").lecturers(Set.of("GV_ECO_01")).build());

        courseRepository.saveAll(courses);

        // Chương trình đào tạo
        Set<ProgramSubject> itSubjects = new HashSet<>();
        itSubjects.add(createSubject("INT101", "Lập trình Java", 3, 1));
        itSubjects.add(createSubject("INT102", "Cấu trúc dữ liệu", 3, 2));
        itSubjects.add(createSubject("INT103", "Lập trình Web", 3, 3));
        itSubjects.add(createSubject("INT104", "Cơ sở dữ liệu", 3, 4));
        itSubjects.add(createSubject("INT105", "Trí tuệ nhân tạo", 3, 5));
        itSubjects.add(createSubject("INT106", "Khóa luận tốt nghiệp", 10, 8));
        EducationProgram programIT = EducationProgram.builder().majorId("M_IT").cohort("2021").subjects(itSubjects).build();

        Set<ProgramSubject> ecoSubjects = new HashSet<>();
        ecoSubjects.add(createSubject("ECO101", "Kinh tế vĩ mô", 3, 1));
        ecoSubjects.add(createSubject("ECO102", "Kinh tế vi mô", 3, 2));
        ecoSubjects.add(createSubject("ECO103", "Marketing căn bản", 3, 3));
        ecoSubjects.add(createSubject("ECO104", "Quản trị Logistics", 3, 4));
        ecoSubjects.add(createSubject("ECO105", "Luật kinh doanh", 3, 5));
        EducationProgram programEco = EducationProgram.builder().majorId("M_LOG").cohort("2023").subjects(ecoSubjects).build();

        programRepository.saveAll(List.of(programIT, programEco));
    }

    private ProgramSubject createSubject(String id, String name, int credits, int semester) {
        return ProgramSubject.builder().courseId(id).courseName(name).subjectType(SubjectType.COMPULSORY).credits(credits).semester(semester).build();
    }

    // ==========================================
    // 4. DATA LỚP HỌC & ENROLLMENT (REALISTIC)
    // ==========================================
    private void initClassesAndEnrollments() {
        List<CourseClass> classes = new ArrayList<>();
        List<StudentEnrollment> enrollments = new ArrayList<>();

        // SV IT (22110254)
        createClassAndEnrollment(classes, enrollments, "CL_JAVA_01", "Lập trình Java", "INT101", "GV_IT_01", "HK1_2023_2024", "2023-2024", STUDENT_IT_ID, 8.0, 9.0, 8.5);
        createClassAndEnrollment(classes, enrollments, "CL_WEB_01", "Lập trình Web", "INT103", "GV_IT_03", "HK2_2023_2024", "2023-2024", STUDENT_IT_ID, 7.5, 8.5, 8.0);
        createClassAndEnrollment(classes, enrollments, "CL_DB_02", "Cơ sở dữ liệu", "INT104", "GV_IT_02", "HK1_2024_2025", "2024-2025", STUDENT_IT_ID, 6.0, 7.0, 6.5);
        createClassAndEnrollment(classes, enrollments, "CL_AI_01", "Trí tuệ nhân tạo", "INT105", "GV_IT_02", "HK2_2024_2025", "2024-2025", STUDENT_IT_ID, 9.0, 9.5, 9.3);
        createClassAndEnrollment(classes, enrollments, "CL_CAPSTONE_01", "Khóa luận tốt nghiệp", "INT106", "GV_IT_01", "HK1_2025_2026", "2025-2026", STUDENT_IT_ID, null, null, null);

        // SV Kinh Tế (22110177)
        createClassAndEnrollment(classes, enrollments, "CL_MACRO_01", "Kinh tế vĩ mô", "ECO101", "GV_ECO_01", "HK1_2023_2024", "2023-2024", STUDENT_ECO_ID, 7.5, 8.0, 7.8);
        createClassAndEnrollment(classes, enrollments, "CL_MICRO_02", "Kinh tế vi mô", "ECO102", "GV_ECO_01", "HK2_2023_2024", "2023-2024", STUDENT_ECO_ID, 8.0, 8.5, 8.3);
        createClassAndEnrollment(classes, enrollments, "CL_MKT_01", "Marketing căn bản", "ECO103", "GV_ECO_01", "HK1_2024_2025", "2024-2025", STUDENT_ECO_ID, 9.0, 9.0, 9.0);
        createClassAndEnrollment(classes, enrollments, "CL_LOG_01", "Quản trị Logistics", "ECO104", "GV_ECO_01", "HK2_2024_2025", "2024-2025", STUDENT_ECO_ID, 6.5, 7.5, 7.0);
        createClassAndEnrollment(classes, enrollments, "CL_LAW_01", "Luật kinh doanh", "ECO105", "GV_ECO_01", "HK1_2025_2026", "2025-2026", STUDENT_ECO_ID, 7.0, null, null);

        courseClassRepository.saveAll(classes);
        enrollmentRepository.saveAll(enrollments);
        System.out.println("   -> Đã tạo: 10 Lớp học (Tên GV chuẩn + Giờ học chuẩn)");
    }

    private void createClassAndEnrollment(List<CourseClass> classes, List<StudentEnrollment> enrollments,
                                          String classId, String className, String courseId, String lecturerId,
                                          String semester, String year, String studentId,
                                          Double midterm, Double finalScore, Double total) {

        int dayRandom = new Random().nextInt(5) + 2;
        String dayOfWeek = getDayName(dayRandom);

        // Random Sáng (1) hoặc Chiều (7)
        boolean isMorning = new Random().nextBoolean();
        int startPeriod = isMorning ? 1 : 7;
        int endPeriod = startPeriod + 2;

        TimeSlot slot = TimeSlot.builder()
                .dayOfWeek(dayOfWeek)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .room("H1-" + (100 + new Random().nextInt(10)))
                .campus("Cơ sở " + (courseId.startsWith("INT") ? "1" : "2"))
                .build();

        // TỰ ĐỘNG ĐIỀN GIỜ
        TimeUtils.enrichTime(slot);

        // MAP TÊN GIẢNG VIÊN CHUẨN XÁC
        String lecturerName = getLecturerName(lecturerId);

        CourseClass cClass = CourseClass.builder()
                .id(classId).name(className)
                .courseId(courseId).courseName(className)
                .lecturerId(lecturerId).lecturerName(lecturerName) // <--- Đã sửa chỗ này, không còn GV Bộ Môn
                .semester(semester).academicYear(year)
                .studentIds(Set.of(studentId))
                .timeSlots(Set.of(slot))
                .build();
        classes.add(cClass);

        StudentEnrollment enrollment = StudentEnrollment.builder()
                .studentId(studentId)
                .courseClassId(classId)
                .semester(semester).academicYear(year)
                .midtermScore(midterm).finalScore(finalScore).totalScore(total)
                .build();
        enrollments.add(enrollment);
    }

    // --- HELPER MỚI: Map ID sang Tên Giảng Viên ---
    private String getLecturerName(String lecturerId) {
        return switch (lecturerId) {
            case "GV_IT_01" -> "TS. Nguyễn Văn Code";
            case "GV_IT_02" -> "PGS. Trần Thị Data";
            case "GV_IT_03" -> "ThS. Lê Văn Job";
            case "GV_ECO_01" -> "TS. Phạm Kinh Tế";
            case "GV_ADMIN" -> "Thầy Quản Lý";
            default -> "Giảng Viên Thỉnh Giảng";
        };
    }

    private String getDayName(int day) {
        return switch (day) {
            case 2 -> "MONDAY";
            case 3 -> "TUESDAY";
            case 4 -> "WEDNESDAY";
            case 5 -> "THURSDAY";
            case 6 -> "FRIDAY";
            case 7 -> "SATURDAY";
            default -> "SUNDAY";
        };
    }
}