package net.flaim.service;

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

    public void register(RegisterRequest registerRequest) throws RuntimeException {

        if (userRepository.existsByUsername(registerRequest.getUsername())) throw new RuntimeException("Username already exists");

        if (userRepository.existsByEmail(registerRequest.getEmail())) throw new RuntimeException("Email already exists");

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) throw new RuntimeException("Passwords don't match");

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}
