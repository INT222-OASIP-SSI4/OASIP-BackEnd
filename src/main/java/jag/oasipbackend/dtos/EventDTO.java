package jag.oasipbackend.dtos;

import jag.oasipbackend.entities.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Integer id;

    private String bookingName;

    private String bookingEmail;

    private Instant eventStartTime;

    private Integer eventDuration;

    private String eventNotes;

    private EventCategory eventCategory;
}
