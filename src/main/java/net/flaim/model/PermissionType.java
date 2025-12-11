package net.flaim.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionType {
    USER_READ("user:read", "View users"),
    USER_DELETE("user:delete", "Delete users");

    private final String permission;
    private final String description;

}