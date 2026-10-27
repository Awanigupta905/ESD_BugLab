package edu.iiitb.esdlab3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than zero")
    private Integer capacity;
}