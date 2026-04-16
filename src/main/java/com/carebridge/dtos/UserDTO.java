package com.carebridge.dtos;

import com.carebridge.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(
    Long id,
    String name,
    String email,
    Role role,
    String displayName,
    String displayEmail,
    String displayPhone,
    String internalEmail,
    String internalPhone,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password
) {
    // Static builder-like method if needed, or just use constructor
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
        private String password;

        public UserDTOBuilder id(Long id) { this.id = id; return this; }
        public UserDTOBuilder name(String name) { this.name = name; return this; }
        public UserDTOBuilder email(String email) { this.email = email; return this; }
        public UserDTOBuilder role(Role role) { this.role = role; return this; }
        public UserDTOBuilder displayName(String displayName) { this.displayName = displayName; return this; }
        public UserDTOBuilder displayEmail(String displayEmail) { this.displayEmail = displayEmail; return this; }
        public UserDTOBuilder displayPhone(String displayPhone) { this.displayPhone = displayPhone; return this; }
        public UserDTOBuilder internalEmail(String internalEmail) { this.internalEmail = internalEmail; return this; }
        public UserDTOBuilder internalPhone(String internalPhone) { this.internalPhone = internalPhone; return this; }
        public UserDTOBuilder password(String password) { this.password = password; return this; }

        public UserDTO build() {
            return new UserDTO(id, name, email, role, displayName, displayEmail, displayPhone, internalEmail, internalPhone, password);
        }
    }
}
