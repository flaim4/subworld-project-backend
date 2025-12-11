package net.flaim.controller;

import net.flaim.annotation.CurrentSession;
import net.flaim.annotation.RequiresPermission;
import net.flaim.dto.*;
import net.flaim.model.PermissionType;
import net.flaim.model.Session;
import net.flaim.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    @RequiresPermission(PermissionType.USER_READ)
    public ResponseEntity<BaseResponse<PaginatedResponse<UserAdminDto>>> getAllUsers(@Validated PaginationRequest request, @CurrentSession Session session) {
        PaginatedResponse<UserAdminDto> users = userManagementService.getAllUsers(request);
        return ResponseEntity.ok(BaseResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionType.USER_READ)
    public ResponseEntity<BaseResponse<UserAdminDto>> getUserById(@PathVariable Long id, @CurrentSession Session session) {
        UserAdminDto user = userManagementService.getUserById(id);
        return ResponseEntity.ok(BaseResponse.success("User retrieved successfully", user));
    }

    @GetMapping("/username/{username}")
    @RequiresPermission(PermissionType.USER_READ)
    public ResponseEntity<BaseResponse<UserAdminDto>> getUserByUsername(@PathVariable String username, @CurrentSession Session session) {
        return ResponseEntity.ok(BaseResponse.success("User retrieved successfully", userManagementService.getUserByUsername(username)));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionType.USER_DELETE)
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id, @CurrentSession Session session) {
        userManagementService.deleteUser(id);
        return ResponseEntity.ok(BaseResponse.success("User deleted successfully", null));
    }

    @DeleteMapping("/batch")
    @RequiresPermission(PermissionType.USER_DELETE)
    public ResponseEntity<BaseResponse<Void>> deleteMultipleUsers(@RequestBody List<Long> userIds, @CurrentSession Session session) {
        userManagementService.deleteMultipleUsers(userIds);
        return ResponseEntity.ok(BaseResponse.success("Users deleted successfully", null));
    }

    @GetMapping("/{id}/permissions")
    @RequiresPermission(PermissionType.USER_READ)
    public ResponseEntity<BaseResponse<Set<String>>> getUserPermissions(@PathVariable Long id, @CurrentSession Session session) {
        return ResponseEntity.ok(BaseResponse.success("User permissions retrieved", userManagementService.getUserPermissions(id).stream().map(Enum::name).collect(Collectors.toSet())));
    }
}