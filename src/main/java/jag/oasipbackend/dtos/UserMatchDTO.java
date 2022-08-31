package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserMatchDTO {

    private String userName;

    @NotNull(message = "password must not null")
    @NotBlank(message = "password must not null")
    @NotEmpty(message = "password must not null")
    private String password;

    @NotNull(message = "userEmail must not null")
    @NotBlank(message = "userEmail must not null")
    @NotEmpty(message = "userEmail must not null")
    @Email(message = "userEmail is invalid email, please input correct email form")
    private String userEmail;

}
