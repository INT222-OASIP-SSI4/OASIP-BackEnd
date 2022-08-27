package jag.oasipbackend.dtos;

import jag.oasipbackend.validators.EnumValidator;
import jag.oasipbackend.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Lob;
import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {

    @NotNull(message = "userName must not null")
    @NotBlank(message = "userName must not null")
    @NotEmpty(message = "userName must not null")
    @Size(min = 1,max = 100,message = "userName must have length between 1-100")
    private String userName;

    @NotNull(message = "password must not null")
    @NotBlank(message = "password must not null")
    @NotEmpty(message = "password must not null")
    @Size(min = 8,max = 14,message = "password must have length between 8-14")
    private String password;

    @NotNull(message = "userEmail must not null")
    @NotBlank(message = "userEmail must not null")
    @NotEmpty(message = "userEmail must not null")
    @Email(message = "userEmail is invalid email, please input correct email form")
    @Size(min = 1,max = 50, message = "userEmail is must not more than 50")
    private String userEmail;

    @Lob
    @NotNull(message = "role must not null")
    @NotBlank(message = "role must not null")
    @NotEmpty(message = "role must not null")
    @EnumValidator(enumClass = RoleType.class)
    private String role;

    public String getUserEmail() {
        return userEmail.trim();
    }

    public void setUserEmail(String userEmail) {this.userEmail = userEmail.trim();}

    public void setRole(String role) {this.role = role.trim();}

    public String getRole(){
        return role.trim();
    }
}
