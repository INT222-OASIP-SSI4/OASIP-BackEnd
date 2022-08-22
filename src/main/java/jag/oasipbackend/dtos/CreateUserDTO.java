package jag.oasipbackend.dtos;

import jag.oasipbackend.validators.EnumValidator;
import jag.oasipbackend.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {

    @NotNull(message = "userName must not null")
    @Size(min = 1,max = 100,message = "userName must have length between 1-100")
    private String userName;

    @Email(message = "userEmail is invalid email, pls input correct email form")
    @NotNull(message = "userEmail must not null")
    @Size(min = 1,max = 100, message = "userEmail is must not more than 50")
    private String userEmail;

    @NotNull(message = "role must not null")
    @EnumValidator(enumClass = RoleType.class)
    private String role;
}
