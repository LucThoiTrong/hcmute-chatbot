package hcmute.edu.vn.hcmutechatbot.model;

import hcmute.edu.vn.hcmutechatbot.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;

    @Indexed(unique = true) // Tối ưu tìm kiếm và tránh trùng lặp
    private String username;

    private String password;
    private String personalEmail;
    private String schoolEmail;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    private String ownerId;

    @Builder.Default
    private boolean isActive = true;
}