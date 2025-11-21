package net.flaim.dto.auth.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be 3-50 characters: English letters, numbers and _")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^[\\x20-\\x7E]{6,}$", message = "Password must be at least 6 characters: English letters, numbers and symbols")
    private String password;
}
