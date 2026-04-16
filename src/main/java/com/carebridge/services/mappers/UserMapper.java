package com.carebridge.services.mappers;

import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.UserDTO;
import com.carebridge.entities.User;

import java.util.Set;

public final class UserMapper {
    private UserMapper() {}

    public static UserDTO toDTO(User u) {
        if (u == null) return null;
        return UserDTO.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .displayName(u.getDisplayName())
                .displayEmail(u.getDisplayEmail())
                .displayPhone(u.getDisplayPhone())
                .internalEmail(u.getInternalEmail())
                .internalPhone(u.getInternalPhone())
                .build();
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User u = new User();
        u.setName(dto.name());
        u.setEmail(dto.email());
        u.setRole(dto.role());
        u.setDisplayName(dto.displayName());
        u.setDisplayEmail(dto.displayEmail());
        u.setDisplayPhone(dto.displayPhone());
        u.setInternalEmail(dto.internalEmail());
        u.setInternalPhone(dto.internalPhone());
        if (dto.password() != null) {
            u.setPassword(dto.password());
        }
        return u;
    }

    public static JwtUserDTO toJwt(User u) {
        if (u == null) return null;
        return JwtUserDTO.builder()
                .username(u.getEmail())
                .roles(Set.of(u.getRole().name()))
                .build();
    }
}
