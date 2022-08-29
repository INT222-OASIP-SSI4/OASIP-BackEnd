package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserMatchDTO {

    private String userName;

    private String password;

    @Email(message = "userEmail is invalid email, please input correct email form")
    private String userEmail;

}
