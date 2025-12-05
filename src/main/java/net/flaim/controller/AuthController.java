package net.flaim.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.flaim.annotation.CurrentSession;
import net.flaim.annotation.RequiresAuth;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.*;
import net.flaim.model.Session;
import net.flaim.repository.SessionRepository;
import net.flaim.service.AuthService;
import net.flaim.service.EmailService;
import net.flaim.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        BaseResponse<String> response = authService.register(request);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        BaseResponse<AuthResponse> response = authService.login(request, httpRequest);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/logout")
    @RequiresAuth
    public ResponseEntity<BaseResponse<Boolean>> logout(@CurrentSession Session session) {
        sessionRepository.delete(session);
        return ResponseEntity.ok(BaseResponse.success("Logged out", true));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<BaseResponse<String>> verifyCode(@Valid @RequestBody MyCodeRequest request) {
        BaseResponse<String> response = emailService.verifyCode(request);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<BaseResponse<String>> sendVerificationCode(@RequestBody Map<String, String> request) throws MessagingException {
        BaseResponse<String> response = emailService.sendCodeEmail(request.get("email"), emailService.generateVerificationCode());
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse<String>> forgotPassword(@RequestParam String email) throws MessagingException {
        BaseResponse<String> response = emailService.sendPasswordResetCode(email);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        BaseResponse<String> response = emailService.resetPassword(request);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}