package net.flaim.service;

import net.flaim.dto.PaginationRequest;
import net.flaim.dto.PaginatedResponse;
import net.flaim.dto.UserAdminDto;
import net.flaim.dto.UserStatsDto;
import net.flaim.model.*;
import net.flaim.repository.UserRepository;
import net.flaim.repository.SkinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final SkinRepository skinRepository;

    @Transactional(readOnly = true)
    public PaginatedResponse<UserAdminDto> getAllUsers(PaginationRequest request) {
        Pageable pageable = createPageable(request);
        Page<User> usersPage;

        if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
            usersPage = userRepository.searchUsers(request.getSearch(), pageable);
        } else if (request.getEmailVerified() != null) {
            usersPage = userRepository.findByVerifyEmail(request.getEmailVerified(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        return PaginatedResponse.fromPage(usersPage.map(this::convertToUserAdminDto));
    }

    @Transactional(readOnly = true)
    public UserAdminDto getUserById(Long id) {
        return convertToUserAdminDto(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

    @Transactional(readOnly = true)
    public UserAdminDto getUserByUsername(String username) {
        return convertToUserAdminDto(userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found with username: " + username)));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new RuntimeException("User not found with id: " + id);
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteMultipleUsers(List<Long> ids) {
        userRepository.deleteAllById(ids);
    }

    @Transactional
    public void toggleEmailVerification(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setVerifyEmail(!user.isVerifyEmail());
        userRepository.save(user);
    }

    @Transactional
    public void assignPermissionToUser(Long userId, PermissionType permissionType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.getPermissions().add(permissionType);
        userRepository.save(user);
    }

    @Transactional
    public void removePermissionFromUser(Long userId, PermissionType permissionType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.getPermissions().remove(permissionType);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserPermissions(Long userId, List<PermissionType> permissions) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.getPermissions().clear();
        user.getPermissions().addAll(permissions);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserStatsDto getUserStats() {
        return UserStatsDto.builder()
                .totalUsers(userRepository.count())
                .verifiedUsers(userRepository.countVerifiedUsers())
                .unverifiedUsers(userRepository.countUnverifiedUsers())
                .usersToday(userRepository.countUsersRegisteredAfter(LocalDateTime.now().minusDays(1)))
                .usersThisWeek(userRepository.countUsersRegisteredAfter(LocalDateTime.now().minusWeeks(1)))
                .usersThisMonth(userRepository.countUsersRegisteredAfter(LocalDateTime.now().minusMonths(1)))
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserAdminDto> getLatestUsers(int limit) {
        return userRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent().stream()
                .map(this::convertToUserAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserAdminDto> searchUsersByName(String query, int limit) {
        return userRepository.searchUsers(query, PageRequest.of(0, limit)).getContent().stream()
                .map(this::convertToUserAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Set<PermissionType> getUserPermissions(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")).getPermissions();
    }

    private Pageable createPageable(PaginationRequest request) {
        return PageRequest.of(request.getPage(), request.getSize(), createSort(request));
    }

    private Sort createSort(PaginationRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) return Sort.by(Sort.Direction.DESC, "createdAt");
        Sort.Direction direction = Sort.Direction.ASC;
        if (request.getSortDirection() != null) {
            direction = "desc".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
        }
        return Sort.by(direction, request.getSortBy());
    }

    private UserAdminDto convertToUserAdminDto(User user) {
        Skin skin = skinRepository.findByUser(user).orElse(null);
        return UserAdminDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .emailVerified(user.isVerifyEmail())
                .createdAt(user.getCreatedAt())
                .skinUrl(skin != null ? skin.getSkinUrl() : null)
                .avatarUrl(skin != null ? skin.getAvatarUrl() : null)
                .skinType(skin != null ? skin.getSkinType().name() : null)
                .permissions(user.getPermissions().stream().map(Enum::name).collect(Collectors.toSet()))
                .build();
    }
}