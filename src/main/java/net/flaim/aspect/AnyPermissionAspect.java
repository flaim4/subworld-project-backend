package net.flaim.aspect;

import lombok.RequiredArgsConstructor;
import net.flaim.annotation.RequiresAnyPermission;
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
public class AnyPermissionAspect extends AuthenticationAspect {

    @Around("@annotation(net.flaim.annotation.RequiresAnyPermission)")
    public Object checkAnyPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Session session = authenticateRequest();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresAnyPermission annotation = signature.getMethod().getAnnotation(RequiresAnyPermission.class);

        boolean hasAnyPermission = false;
        for (PermissionType permission : annotation.value()) {
            if (session.getUser().hasPermission(permission)) {
                hasAnyPermission = true;
                break;
            }
        }

        if (!hasAnyPermission) throw new UnauthorizedException("Insufficient permissions. Required any of: " + String.join(", ", java.util.Arrays.stream(annotation.value()).map(Enum::name).toArray(String[]::new)));

        return proceedWithSession(joinPoint, session);
    }
}