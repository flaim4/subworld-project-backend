package net.flaim.repository;

import net.flaim.model.EmailVerification;
import net.flaim.model.Session;
import net.flaim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
}
