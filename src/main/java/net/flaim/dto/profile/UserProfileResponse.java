package net.flaim.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileResponse {
    private String username;
    private String skinUrl;
    private String avatarUrl;
}
