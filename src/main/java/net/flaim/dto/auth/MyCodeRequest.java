package net.flaim.dto.auth;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class MyCodeRequest {

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Code is required")
    @Min(value = 100000, message = "Code must be 6 digits")
    @Max(value = 999999, message = "Code must be 6 digits")
    private Integer code;
}