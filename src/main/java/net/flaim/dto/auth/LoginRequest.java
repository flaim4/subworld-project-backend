package net.flaim.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Pattern(regexp = "^(?:[a-zA-Z0-9_]{3,50}|[^@\\s]+@[^@\\s]+\\.[^@\\s]+)$",
            message = "Username must be 3-50 characters (letters, numbers, _) or valid email")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^[\\x20-\\x7E]{6,}$",
            message = "Password must be at least 6 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}