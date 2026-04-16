package com.carebridge.dtos;

import com.carebridge.enums.Role;

public record RegisterUserDTO(
    String name,
    String email,
    String password,
    String displayName,
    String displayEmail,
    String displayPhone,
    String internalEmail,
    String internalPhone,
    Role role
) {}
