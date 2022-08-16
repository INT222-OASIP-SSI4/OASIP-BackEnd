package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class EventCategoryDTO {
    private Integer id;

    private String eventCategoryName;

    private String eventCategoryDescription;

    private Integer eventDuration;
}
