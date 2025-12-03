package net.flaim.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.flaim.annotation.RequiresAuth;
import net.flaim.annotation.CurrentSession;
import net.flaim.exception.UnauthorizedException;
import net.flaim.model.Session;
import net.flaim.repository.SessionRepository;
import net.flaim.service.SessionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthAspect {

    private final SessionRepository sessionRepository;

    @Around("@annotation(net.flaim.annotation.RequiresAuth)")
    public Object checkAuth(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) throw new UnauthorizedException("Authorization header is required");

        if (!authHeader.startsWith("Bearer ")) throw new UnauthorizedException("Invalid authorization format. " + "Expected format: 'Bearer <token>'. " + "Received: '" + authHeader + "'");

        String token = authHeader.substring(7).trim();

        if (token.isBlank()) throw new UnauthorizedException("Token is empty after 'Bearer '");

        Session session = sessionRepository.findByToken(token).orElseThrow(() -> new UnauthorizedException("Invalid or expired token. " + "Please login again."));

        if (!session.isActive()) throw new UnauthorizedException("Session has expired. Please login again.");

        session.setLastAccessedAt(java.time.LocalDateTime.now());
        sessionRepository.save(session);

        Object[] args = injectSession(joinPoint, session);

        return joinPoint.proceed(args);
    }

    private Object[] injectSession(ProceedingJoinPoint joinPoint, Session session) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(CurrentSession.class)) {
                args[i] = session;
                break;
            }
        }

        return args;
    }
}