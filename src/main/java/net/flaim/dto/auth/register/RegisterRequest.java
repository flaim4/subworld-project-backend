package net.flaim.dto.auth.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be 3-50 characters: English letters, numbers and _")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^[\\x20-\\x7E]{6,}$", message = "Password must be at least 6 characters: English letters, numbers and symbols")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}