package net.flaim.repository;

import net.flaim.model.Session;
import net.flaim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByToken(String token);
    boolean existsByToken(String token);

    void deleteByToken(String token);
}
