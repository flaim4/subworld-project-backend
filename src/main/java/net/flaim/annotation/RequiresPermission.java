package net.flaim.annotation;

import net.flaim.model.PermissionType;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    PermissionType value();
}