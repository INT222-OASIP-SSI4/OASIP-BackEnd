package jag.oasipbackend.enums;

import org.springframework.security.core.GrantedAuthority;

public enum RoleType implements GrantedAuthority {
        admin, student, lecturer;

        @Override
        public String getAuthority() {
                return name();
        }
}