package net.flaim.dto.auth;

import lombok.Data;

@Data
public class MyCodeRequest {
    private final String email;
    private final int code;
}
