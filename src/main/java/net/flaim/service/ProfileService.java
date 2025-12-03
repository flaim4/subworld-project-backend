package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.profile.UserProfileResponse;
import net.flaim.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    public BaseResponse<UserProfileResponse> getUserProfile(User user) {
        return null;
    }
}
