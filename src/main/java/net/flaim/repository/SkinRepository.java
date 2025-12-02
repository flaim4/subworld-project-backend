package net.flaim.repository;

import net.flaim.model.Session;
import net.flaim.model.Skin;
import net.flaim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkinRepository extends JpaRepository<Skin, Long> {
    Optional<Skin> findByUser(User user);
}
