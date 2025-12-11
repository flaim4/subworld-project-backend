package net.flaim.dto.auth;

import lombok.Data;
import net.flaim.model.PermissionType;

import java.util.List;

@Data
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private List<PermissionType> Permissions;
}