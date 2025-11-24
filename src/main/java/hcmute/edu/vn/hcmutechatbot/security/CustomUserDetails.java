package hcmute.edu.vn.hcmutechatbot.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hcmute.edu.vn.hcmutechatbot.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Data
public class CustomUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String ownerId;
    private String fullName;

    @JsonIgnore
    private String password;

    // Danh sách quyền
    private Collection<? extends GrantedAuthority> authorities;

    // Method chuyển Account -> CustomUserDetails
    public static CustomUserDetails build(Account account, String fullName) {
        // Chuyển Enum Role thành GrantedAuthority để spring security hiểu user có quyền gì.
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + account.getRole().name())
        );

        return new CustomUserDetails(
                account.getId(),
                account.getUsername(),
                account.getOwnerId(),
                fullName,
                account.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Các hàm kiểm tra trạng thái account
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
