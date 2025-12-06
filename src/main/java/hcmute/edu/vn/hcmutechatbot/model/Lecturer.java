package hcmute.edu.vn.hcmutechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "lecturers")
public class Lecturer {
    @Id
    private String id;

    private String fullName;
    private String email;

    private String facultyId;
    private String facultyName;
}