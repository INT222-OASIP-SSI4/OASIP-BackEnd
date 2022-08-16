package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UpdateEventCategoryDTO {

    @NotNull(message = "eventCategoryName is required")
    @Size(min = 1, max = 100, message = "eventCategoryName must have length between 1-100")
    @NotBlank(message = "eventCategoryName is required")
    @NotEmpty(message = "eventCategoryName is required")
    private String eventCategoryName;

    @Size(min = 0, max = 500,message = "eventCategoryDescription must have length between 0-500")
    private String eventCategoryDescription;

    @NotNull(message = "eventDuration is required")
    @Min(value = 1,message = "eventDuration must have value between 1-480")
    @Max(value = 480, message = "eventDuration must have value between 1-480")
    private Integer eventDuration;
    
}
