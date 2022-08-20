package jag.oasipbackend.dtos;

import jag.oasipbackend.entities.EventCategory;
import jag.oasipbackend.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserDTO {
    private Integer id;

    private String userName;

    private String userEmail;

    private RoleType role;

}
