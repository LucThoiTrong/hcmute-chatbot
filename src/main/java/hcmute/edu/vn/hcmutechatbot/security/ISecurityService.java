package hcmute.edu.vn.hcmutechatbot.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

public interface ISecurityService {
    private CustomUserDetails getUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    default String getCurrentUserId() {
        CustomUserDetails userDetails = getUser();
        return userDetails.getUsername(); // (Chính là id đăng nhập của sinh viên và giảng viên)
    }

    default List<String> getAuthorities() {
        CustomUserDetails userDetails = getUser();
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
