package net.flaim.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.dto.auth.login.LoginRequest;
import net.flaim.dto.auth.register.RegisterRequest;
import net.flaim.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        BaseResponse<AuthResponse> response = authService.register(request);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        BaseResponse<AuthResponse> response = authService.login(request);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}