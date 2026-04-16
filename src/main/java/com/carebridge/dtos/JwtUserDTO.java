package com.carebridge.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;

public record JwtUserDTO(
    String username,
    @JsonIgnore
    String password,
    Set<String> roles
) {
    public static JwtUserDTOBuilder builder() {
        return new JwtUserDTOBuilder();
    }

    public static class JwtUserDTOBuilder {
        private String username;
        private String password;
        private Set<String> roles;

        public JwtUserDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public JwtUserDTOBuilder password(String password) {
            this.password = password;
            return this;
        }

        public JwtUserDTOBuilder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public JwtUserDTO build() {
            return new JwtUserDTO(username, password, roles);
        }
    }
}
