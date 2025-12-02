package net.flaim.repository;

import net.flaim.model.Session;
import net.flaim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByToken(String token);
    boolean existsByToken(String token);

    void deleteByToken(String token);
}
