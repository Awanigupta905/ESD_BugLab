package edu.iiitb.esdlab3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRegistrationRequest {

    @NotBlank(message = "Student email is required")
    @Email(message = "Student email must be valid")
    private String studentEmail;
}