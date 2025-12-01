package net.flaim.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.dto.auth.LoginRequest;
import net.flaim.dto.auth.MyCodeRequest;
import net.flaim.dto.auth.RegisterRequest;
import net.flaim.service.AuthService;
import net.flaim.service.EmailService;
import net.flaim.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        BaseResponse<String> response = authService.register(request, httpRequest);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        BaseResponse<AuthResponse> response = authService.login(request, httpRequest);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Boolean>> logout(@RequestHeader("Authorization") String token) {
        BaseResponse<Boolean> response = authService.logout(token.replace("Bearer", "").trim());
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/myCode")
    public ResponseEntity<BaseResponse<String>> myCode(@Valid @RequestBody MyCodeRequest myCodeRequest) {
        BaseResponse<String> response = emailService.myCode(myCodeRequest);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PostMapping("/sendCode")
    public ResponseEntity<BaseResponse<String>> sendCode(@RequestBody Map<String, String> request) throws MessagingException {
        BaseResponse<String> response = emailService.sendCodeEmail(request.get("email"), emailService.generateVerificationCode());
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

}