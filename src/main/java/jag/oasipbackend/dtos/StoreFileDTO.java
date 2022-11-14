package jag.oasipbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreFileDTO {

    private String fileName;

    private String downloadUrl;

    @Size(max = 10485760, message = "The file size cannot be larger than 10 MB.")
    private Integer fileSize;
}
