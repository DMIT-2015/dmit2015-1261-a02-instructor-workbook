package dmit2015.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;


@Data

public class TodoItemDto implements Serializable {

    private Long id;


    @NotBlank (message = "Name can not be empty")
    private String Name;

    private  boolean complete;

    private  Integer version;
}
