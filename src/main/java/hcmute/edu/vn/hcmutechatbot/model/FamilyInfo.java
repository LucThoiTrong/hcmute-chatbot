package hcmute.edu.vn.hcmutechatbot.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyInfo {
    private String contactPersonName; // hoTenNguoiLienHe
    private String contactPersonPhone; // dienThoaiNguoiLienHe
    private String contactPersonAddress; // diaChiNguoiLienHe
    private String fatherName; // hoTenCha
    private String fatherPhone; // dienThoaiCha
    private String motherName; // hoTenMe
    private String motherPhone; // dienThoaiMe
}
