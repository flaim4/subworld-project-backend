package net.flaim.repository;

import net.flaim.model.EmailVerification;
import net.flaim.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<EmailVerification, Long> {
}
