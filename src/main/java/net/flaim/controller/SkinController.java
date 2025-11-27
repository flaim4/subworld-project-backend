package net.flaim.controller;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.model.User;
import net.flaim.repository.SessionRepository;
import net.flaim.service.SessionService;
import net.flaim.service.SkinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/skin")
@RequiredArgsConstructor
public class SkinController {

    private final SkinService skinService;
    private final SessionService sessionService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<Boolean>> upload(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        BaseResponse<Boolean> response = skinService.uploadSkin(sessionService.getUser(token), file);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}
