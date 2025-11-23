package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactPerson {
    private String contactName;
    private String contactPhone;
    private String contactAddress;

    private String fatherName;
    private String fatherPhone;

    private String motherName;
    private String motherPhone;
}
