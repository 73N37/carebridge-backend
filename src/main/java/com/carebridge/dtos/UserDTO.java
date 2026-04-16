package com.carebridge.dtos;

import com.carebridge.entities.enums.Role;
import java.util.Objects;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;

    private String displayName;
    private String displayEmail;
    private String displayPhone;
    private String internalEmail;
    private String internalPhone;

    public UserDTO() {}

    public UserDTO(Long id, String name, String email, Role role, String displayName, String displayEmail, String displayPhone, String internalEmail, String internalPhone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.displayName = displayName;
        this.displayEmail = displayEmail;
        this.displayPhone = displayPhone;
        this.internalEmail = internalEmail;
        this.internalPhone = internalPhone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDisplayEmail() { return displayEmail; }
    public void setDisplayEmail(String displayEmail) { this.displayEmail = displayEmail; }
    public String getDisplayPhone() { return displayPhone; }
    public void setDisplayPhone(String displayPhone) { this.displayPhone = displayPhone; }
    public String getInternalEmail() { return internalEmail; }
    public void setInternalEmail(String internalEmail) { this.internalEmail = internalEmail; }
    public String getInternalPhone() { return internalPhone; }
    public void setInternalPhone(String internalPhone) { this.internalPhone = internalPhone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDTO userDTO)) return false;
        return Objects.equals(id, userDTO.id) && Objects.equals(name, userDTO.name) && Objects.equals(email, userDTO.email) && role == userDTO.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, role);
    }

    @Override
    public String toString() {
        return "UserDTO{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", role=" + role + '}';
    }

    public static UserDTOBuilder builder() {
        return new UserDTOBuilder();
    }

    public static class UserDTOBuilder {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private String displayName;
        private String displayEmail;
        private String displayPhone;
        private String internalEmail;
        private String internalPhone;

        public UserDTOBuilder id(Long id) { this.id = id; return this; }
        public UserDTOBuilder name(String name) { this.name = name; return this; }
        public UserDTOBuilder email(String email) { this.email = email; return this; }
        public UserDTOBuilder role(Role role) { this.role = role; return this; }
        public UserDTOBuilder displayName(String displayName) { this.displayName = displayName; return this; }
        public UserDTOBuilder displayEmail(String displayEmail) { this.displayEmail = displayEmail; return this; }
        public UserDTOBuilder displayPhone(String displayPhone) { this.displayPhone = displayPhone; return this; }
        public UserDTOBuilder internalEmail(String internalEmail) { this.internalEmail = internalEmail; return this; }
        public UserDTOBuilder internalPhone(String internalPhone) { this.internalPhone = internalPhone; return this; }

        public UserDTO build() {
            return new UserDTO(id, name, email, role, displayName, displayEmail, displayPhone, internalEmail, internalPhone);
        }
    }
}
