package hcmute.edu.vn.hcmutechatbot.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    private String id;
    private String studentCode; // maSoSinhVien
    private String fullName; // hoTen
    private LocalDate dateOfBirth; // ngaySinh
    private String placeOfBirth; // noiSinh
    private String gender; // gioiTinh
    private String nationalId; // canCuocCongDan
    private String ethnicGroup; // danToc
    private String religion; // tonGiao
    private ContactInfo contactInfo;
    private AcademicInfo academicInfo;
    private FamilyInfo familyInfo;
}
