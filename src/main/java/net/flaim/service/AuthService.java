package net.flaim.service;

import jakarta.servlet.http.HttpServletRequest;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.dto.auth.LoginRequest;
import net.flaim.repository.SessionRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.auth.RegisterRequest;
import net.flaim.model.User;
import net.flaim.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final SkinService skinService;

    public BaseResponse<AuthResponse> register(RegisterRequest registerRequest, HttpServletRequest httpRequest) {
        try {
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return BaseResponse.error("Username already exists");
            }

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return BaseResponse.error("Email already exists");
            }

            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return BaseResponse.error("Passwords don't match");
            }

            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            userRepository.save(user);

            String token = jwtService.generateToken(user.getUsername());

            sessionService.createSession(user, token, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
            skinService.createDefault(user);
            return BaseResponse.success("User registered successfully", new AuthResponse(token, user.getUsername()));

        } catch (Exception e) {
            return BaseResponse.error("Registration failed: " + e.getMessage());
        }
    }

    public BaseResponse<AuthResponse> login(LoginRequest loginRequest, HttpServletRequest httpRequest) {
        try {
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername()).orElse(null);

            if (user == null) {
                return BaseResponse.error("User not found");
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return BaseResponse.error("Invalid password");
            }

            String token = jwtService.generateToken(user.getUsername());

            sessionService.createSession(user, token, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
            return BaseResponse.success("Login successful", new AuthResponse(token, user.getUsername()));

        } catch (Exception e) {
            return BaseResponse.error("Login failed: " + e.getMessage());
        }
    }

    public BaseResponse<Void> logout(@RequestHeader("Authorization") String token) {
        if (!sessionRepository.existsByToken(token)) return BaseResponse.error("Session not found");
        sessionRepository.findByToken(token).ifPresent(sessionRepository::delete);
        return BaseResponse.success();
    }
}