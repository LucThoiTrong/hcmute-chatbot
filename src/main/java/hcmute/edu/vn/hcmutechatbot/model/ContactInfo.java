package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactInfo {
    private String country;
    private String province;
    private String ward;
    private String streetAddress;
    private String mobilePhone;
    private String schoolEmail;
    private String personalEmail;
}
