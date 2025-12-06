package net.flaim.controller;

import lombok.RequiredArgsConstructor;
import net.flaim.annotation.CurrentSession;
import net.flaim.annotation.RequiresAuth;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.profile.UserProfileResponse;
import net.flaim.model.Session;
import net.flaim.model.User;
import net.flaim.service.ProfileService;
import net.flaim.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final SessionService sessionService;

    @GetMapping("/me")
    @RequiresAuth
    public ResponseEntity<BaseResponse<UserProfileResponse>> getProfile(@CurrentSession Session session) {
        BaseResponse<UserProfileResponse> response = profileService.getUserProfile(session.getUser());
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}
