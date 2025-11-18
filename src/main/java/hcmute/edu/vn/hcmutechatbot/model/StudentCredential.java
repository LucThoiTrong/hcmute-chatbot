package hcmute.edu.vn.hcmutechatbot.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCredential {
    @Id
    private String id;
    private String studentId;
    private String universityEmail; // emailTruong
    private String personalEmail; // emailCaNhan
    private String hashedPassword;
    private boolean isActive = true;
}
