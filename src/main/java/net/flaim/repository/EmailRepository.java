package net.flaim.repository;

import net.flaim.model.EmailVerification;
import net.flaim.model.Session;
import net.flaim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
}
