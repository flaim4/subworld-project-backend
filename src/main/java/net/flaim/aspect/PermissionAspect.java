package net.flaim.aspect;

import lombok.RequiredArgsConstructor;
import net.flaim.annotation.RequiresPermission;
import net.flaim.exception.UnauthorizedException;
import net.flaim.model.PermissionType;
import net.flaim.model.Session;
import net.flaim.model.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect extends AuthenticationAspect {

    @Around("@annotation(net.flaim.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Session session = authenticateRequest();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresPermission annotation = signature.getMethod().getAnnotation(RequiresPermission.class);

        checkUserPermission(session.getUser(), annotation.value());

        return proceedWithSession(joinPoint, session);
    }

    private void checkUserPermission(User user, PermissionType requiredPermission) {
        if (!user.hasPermission(requiredPermission)) throw new UnauthorizedException("Insufficient permissions. Required: " + requiredPermission.name());
    }
}