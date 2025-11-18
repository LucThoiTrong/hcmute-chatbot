package hcmute.edu.vn.hcmutechatbot.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    private String country; // quocGia
    private String province; // tinhThanh
    private String ward; // phuong
    private String address; // diaChi
    private String mobilePhone; // diDong
    private String universityEmail; // emailTruong
    private String personalEmail; // emailCaNhan
}
