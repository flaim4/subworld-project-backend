package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.dto.auth.LoginRequest;
import net.flaim.dto.auth.RegisterRequest;
import net.flaim.model.User;
import net.flaim.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public BaseResponse<String> register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return BaseResponse.error("Passwords do not match");
        }

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return BaseResponse.error("User with this email already exists");
        }

        Optional<User> existingUsername = userRepository.findByUsername(request.getUsername());
        if (existingUsername.isPresent()) {
            return BaseResponse.error("Username already taken");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setVerifyEmail(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return BaseResponse.success("Registration successful. Please verify your email.");
    }

    public BaseResponse<AuthResponse> login(LoginRequest request) {
        Optional<User> user = findUserByIdentifier(request.getUsername());

        if (user.isEmpty()) {
            return BaseResponse.error("Invalid credentials");
        }

        User foundUser = user.get();

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            return BaseResponse.error("Invalid credentials");
        }

        if (!foundUser.isVerifyEmail()) {
            return BaseResponse.error("Please verify your email first");
        }

        String token = generateAuthToken(foundUser);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUsername(foundUser.getUsername());
        authResponse.setEmail(foundUser.getEmail());

        return BaseResponse.success(authResponse);
    }

    public BaseResponse<Boolean> logout(String token) {
        return BaseResponse.success(true);
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier);
        } else {
            return userRepository.findByUsername(identifier);
        }
    }

    private String generateAuthToken(User user) {
        String rawToken = user.getId() + ":" + System.currentTimeMillis() + ":" + user.getUsername();
        return java.util.Base64.getEncoder().encodeToString(rawToken.getBytes());
    }
}