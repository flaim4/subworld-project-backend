package net.flaim.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.dto.auth.LoginRequest;
import net.flaim.dto.auth.RegisterRequest;
import net.flaim.model.Session;
import net.flaim.model.User;
import net.flaim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.token.secret}")
    private String tokenSecret;

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    private String generateSecureAuthToken(User user) {
        String payload = user.getId() + ":" + System.currentTimeMillis();
        String token = payload + ":" + calculateHmac(payload, tokenSecret);
        return Base64.getUrlEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private String calculateHmac(String data, String key) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmac.doFinal(data.getBytes(StandardCharsets.UTF_8))) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public BaseResponse<String> register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) return BaseResponse.error("Passwords do not match");

        if (userRepository.findByEmail(request.getEmail()).isPresent()) return BaseResponse.error("User with this email already exists");

        if (userRepository.findByUsername(request.getUsername()).isPresent()) return BaseResponse.error("Username already taken");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerifyEmail(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return BaseResponse.success("Registration successful. Please verify your email.");
    }

    public BaseResponse<AuthResponse> login(LoginRequest request, HttpServletRequest httpRequest) {
        Optional<User> user = findUserByIdentifier(request.getUsername());

        if (user.isEmpty()) return BaseResponse.error("Invalid credentials");

        User foundUser = user.get();

        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) return BaseResponse.error("Invalid credentials");

        if (!foundUser.isVerifyEmail()) return BaseResponse.error("Please verify your email first");

        String token = generateSecureAuthToken(foundUser);

        sessionService.createSession(foundUser, token, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUsername(foundUser.getUsername());
        authResponse.setEmail(foundUser.getEmail());

        return BaseResponse.success(authResponse);
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        return identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                : userRepository.findByUsername(identifier);
    }
}