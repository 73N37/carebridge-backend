package com.carebridge.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class JwtUserDTO {
    private String username;
    @JsonIgnore
    private String password;
    private Set<String> roles = new HashSet<>();

    public JwtUserDTO() {
    }

    public JwtUserDTO(String username, String password, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles != null ? roles : new HashSet<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public static JwtUserDTOBuilder builder() {
        return new JwtUserDTOBuilder();
    }

    public static class JwtUserDTOBuilder {
        private String username;
        private String password;
        private Set<String> roles = new HashSet<>();

        JwtUserDTOBuilder() {
        }

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

        @Override
        public String toString() {
            return "JwtUserDTOBuilder{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", roles=" + roles +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtUserDTO that = (JwtUserDTO) o;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, roles);
    }

    @Override
    public String toString() {
        return "JwtUserDTO{" +
                "username='" + username + '\'' +
                ", password='" + (password != null ? "********" : "null") + '\'' +
                ", roles=" + roles +
                '}';
    }
}
