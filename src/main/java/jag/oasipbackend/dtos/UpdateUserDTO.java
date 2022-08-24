package jag.oasipbackend.dtos;

import jag.oasipbackend.enums.RoleType;
import jag.oasipbackend.validators.EnumValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {

    @NotNull(message = "userName must not null")
    @NotBlank(message = "userName must not null")
    @NotEmpty(message = "userName must not null")
    @Size(min = 1,max = 100,message = "userName must have length between 1-100")
    private String userName;

    @Email(message = "userEmail is invalid email, pls input correct email form")
    @NotNull(message = "userEmail must not null")
    @NotBlank(message = "userEmail must not null")
    @NotEmpty(message = "userEmail must not null")
    @Size(min = 1,max = 50, message = "userEmail is must not more than 50")
    private String userEmail;

    @NotNull(message = "role must not null")
    @NotBlank(message = "role must not null")
    @NotEmpty(message = "role must not null")
    @EnumValidator(enumClass = RoleType.class)
    private String role;

    public void setUserName(String userName) {
        this.userName = userName.trim();
    }

    public String getUserName(){
        return userName.trim();
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail.trim();
    }

    public String getUserEmail(){
        return userEmail.trim();
    }

    public void setRole(String role) {this.role = role.trim();}

    public String getRole(){
        return role.trim();
    }
}
