package net.flaim.service;

import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.dto.auth.login.LoginRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.auth.register.RegisterRequest;
import net.flaim.model.User;
import net.flaim.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public BaseResponse<AuthResponse> register(RegisterRequest registerRequest) {
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
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);

            return BaseResponse.success("User registered successfully", new AuthResponse(jwtService.generateToken(user.getUsername()), user.getUsername()));

        } catch (Exception e) {
            return BaseResponse.error("Registration failed: " + e.getMessage());
        }
    }

    public BaseResponse<AuthResponse> login(LoginRequest loginRequest) {
        try {
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername()).orElse(null);

            if (user == null) {
                return BaseResponse.error("User not found");
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return BaseResponse.error("Invalid password");
            }

            return BaseResponse.success("Login successful", new AuthResponse(jwtService.generateToken(user.getUsername()), user.getUsername()));

        } catch (Exception e) {
            return BaseResponse.error("Login failed: " + e.getMessage());
        }
    }
}