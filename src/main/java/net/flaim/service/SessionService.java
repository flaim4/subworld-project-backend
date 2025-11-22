package net.flaim.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.AuthResponse;
import net.flaim.model.Session;
import net.flaim.model.User;
import net.flaim.repository.SessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {

    @Value("${jwt.expiration}")
    private int sessionDurationHours;

    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public void createSession(User user, String token, String ipAddress, String userAgent) {
        Session session = new Session();
        session.setUser(user);
        session.setToken(token);
        session.setUserAgent(userAgent);

        session.setExpiresAt(LocalDateTime.now().plusHours(sessionDurationHours));

        sessionRepository.save(session);
    }
}
