package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.profile.UserProfileResponse;
import net.flaim.model.Skin;
import net.flaim.model.User;
import net.flaim.repository.SkinRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    @Value("${app.skins.url-prefix}")
    private String skinsUrlPrefix;

    @Value("${app.skins.default-skin}")
    private String defaultSkin;

    @Value("${app.avatars.url-prefix}")
    private String avatarsUrlPrefix;

    @Value("${app.avatars.default}")
    private String defaultAvatar;

    private final SkinRepository skinRepository;
    private final AvatarService avatarService;

    public BaseResponse<UserProfileResponse> getUserProfile(User user) {
        try {
            UserProfileResponse response = new UserProfileResponse();
            response.setUsername(user.getUsername());
            response.setSkinUrl(getUserSkinUrl(user));
            response.setAvatarUrl(avatarService.getAvatarUrl(user));

            return BaseResponse.success(response);

        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to load profile");
        }
    }

    public String getUserSkinUrl(User user) {
        return skinRepository.findByUser(user)
                .map(Skin::getSkinUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .orElse(skinsUrlPrefix + "/" + defaultSkin);
    }
}