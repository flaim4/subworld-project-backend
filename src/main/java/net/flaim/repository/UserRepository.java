package net.flaim.repository;

import net.flaim.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE " + "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " + "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    Page<User> findByVerifyEmail(boolean verifyEmail, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.verifyEmail = true")
    long countVerifiedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.verifyEmail = false")
    long countUnverifiedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersRegisteredAfter(@Param("date") LocalDateTime date);
}