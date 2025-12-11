package net.flaim.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthAspect extends AuthenticationAspect {

    @Around("@annotation(net.flaim.annotation.RequiresAuth)")
    public Object checkAuth(ProceedingJoinPoint joinPoint) throws Throwable {
        return proceedWithSession(joinPoint, authenticateRequest());
    }
}