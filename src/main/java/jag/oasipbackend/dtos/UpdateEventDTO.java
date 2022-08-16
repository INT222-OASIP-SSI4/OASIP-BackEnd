package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventDTO {

    @Future(message = "eventStartTime must be a future time")
    private Instant eventStartTime;

    @Size(min = 0, max = 500, message = "eventNotes must have length between 0-500")
    private String eventNotes;

}
