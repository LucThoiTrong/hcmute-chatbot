package hcmute.edu.vn.hcmutechatbot.controller;

import hcmute.edu.vn.hcmutechatbot.dto.request.LoginRequest;
import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import hcmute.edu.vn.hcmutechatbot.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // B1: Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // B2: Nếu đăng nhập thành công
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // B3: Sinh token
        String token = jwtUtils.generateJwtToken(authentication);

        // B4: Lấy thông tin trong UserDetails để trả về token
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(JwtResponse.builder()
                .token(token)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .ownerId(userDetails.getOwnerId())
                .roles(roles)
                .build()
        );
    }
}
