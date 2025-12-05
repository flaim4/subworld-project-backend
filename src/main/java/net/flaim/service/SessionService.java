package net.flaim.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import net.flaim.model.Session;
import net.flaim.model.User;
import net.flaim.repository.SessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    @Value("${app.token.expiration-hours}")
    private int sessionDurationHours;

    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Session createSession(User user, String token, String ipAddress, String userAgent) {
        Session session = new Session();
        session.setUser(user);
        session.setToken(passwordEncoder.encode(token));
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        session.setExpiresAt(LocalDateTime.now().plusHours(sessionDurationHours));

        return sessionRepository.save(session);
    }

    @Transactional
    public Optional<Session> validateAndGetSession(String token) {
        Optional<Session> sessionOpt = sessionRepository.findAll().stream()
                .filter(Session::isActive)
                .filter(session -> passwordEncoder.matches(token, session.getToken()))
                .findFirst();

        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            session.setLastAccessedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }

        return sessionOpt;
    }
}