package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    private String userName;

    @NotNull(message = "password must not null")
    @NotBlank(message = "password must not null")
    @NotEmpty(message = "password must not null")
    private String password;

    @NotNull(message = "userEmail must not null")
    @NotBlank(message = "userEmail must not null")
    @NotEmpty(message = "userEmail must not null")
    @Email(message = "userEmail is invalid email, please input correct email form")
    @Size(min = 1,max = 50, message = "userEmail is must not more than 50")
    private String userEmail;

}
