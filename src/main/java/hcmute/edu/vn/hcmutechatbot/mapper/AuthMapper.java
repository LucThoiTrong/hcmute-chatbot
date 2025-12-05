package hcmute.edu.vn.hcmutechatbot.mapper;

import hcmute.edu.vn.hcmutechatbot.dto.response.JwtResponse;
import hcmute.edu.vn.hcmutechatbot.security.CustomUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    // CASE 1: LOGIN (Sửa target="token" thành target="accessToken")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "type", constant = "Bearer")
    @Mapping(target = "id", source = "userDetails.id")
    @Mapping(target = "username", source = "userDetails.username")
    @Mapping(target = "fullName", source = "userDetails.fullName")
    @Mapping(target = "ownerId", source = "userDetails.ownerId")
    @Mapping(target = "roles", source = "userDetails.authorities", qualifiedByName = "mapAuthoritiesToRoles")
    JwtResponse toJwtResponse(String accessToken, String refreshToken, CustomUserDetails userDetails);

    // CASE 2: REFRESH TOKEN (Sửa target="token" thành target="accessToken")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "type", constant = "Bearer")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    JwtResponse toJwtResponse(String accessToken, String refreshToken);

    @Named("mapAuthoritiesToRoles")
    default List<String> mapAuthoritiesToRoles(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) return null;
        return authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }
}