package net.flaim.dto.auth.register;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RegisterResponse {
    private final String message;
    private final boolean success;

    public static RegisterResponse success(String message) {
        return new RegisterResponse(message, true);
    }

    public static RegisterResponse error(String message) {
        return new RegisterResponse(message, false);
    }
}