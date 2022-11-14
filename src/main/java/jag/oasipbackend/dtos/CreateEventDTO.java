package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventDTO {

    @Future(message = "eventStartTime must be a future time")
    @NotNull(message = "eventStartTime must not null")
    private Instant eventStartTime;

    @Size(min = 0, max = 500, message = "eventNotes must have length between 0-500")
    private String eventNotes;

    @NotNull(message = "bookingName must not null")
    @Size(min = 1,max = 100,message = "bookingName must have length between 1-100")
    private String bookingName;

    @Email(message = "bookingEmail is invalid email, pls input correct email form")
    @NotNull(message = "bookingEmail must not null")
    @Size(min = 1,max = 100, message = "bookingEmail is must not more than 100")
    private String bookingEmail;

    @NotNull(message = "eventCategoryId must not null")
    private Integer eventCategoryId;

}
