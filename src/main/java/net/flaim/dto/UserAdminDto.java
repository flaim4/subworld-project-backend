package net.flaim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDto {
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private String skinUrl;
    private String avatarUrl;
    private String skinType;
    private Set<String> permissions;
}