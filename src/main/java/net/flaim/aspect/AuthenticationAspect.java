package net.flaim.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.flaim.annotation.CurrentSession;
import net.flaim.exception.UnauthorizedException;
import net.flaim.model.Session;
import net.flaim.service.SessionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Component
@NoArgsConstructor
@AllArgsConstructor
public abstract class AuthenticationAspect {

    @Autowired
    protected SessionService sessionService;

    protected Session authenticateRequest() {
        String authHeader = getAuthHeader();

        validateAuthHeader(authHeader);

        String token = extractToken(authHeader);

        Optional<Session> sessionOpt = sessionService.validateAndGetSession(token);

        if (sessionOpt.isEmpty()) throw new UnauthorizedException("Invalid or expired token. Please login again.");

        return sessionOpt.get();
    }

    protected String getAuthHeader() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        return request.getHeader("Authorization");
    }

    protected void validateAuthHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) throw new UnauthorizedException("Authorization header is required");

        if (!authHeader.startsWith("Bearer ")) throw new UnauthorizedException("Invalid authorization format. Expected format: 'Bearer <token>'. " + "Received: '" + authHeader + "'");
    }

    protected String extractToken(String authHeader) {
        String token = authHeader.substring(7).trim();

        if (token.isBlank()) throw new UnauthorizedException("Token is empty after 'Bearer '");

        return token;
    }

    protected Object proceedWithSession(ProceedingJoinPoint joinPoint, Session session) throws Throwable {
        Object[] args = injectSession(joinPoint, session);
        return joinPoint.proceed(args);
    }

    protected Object[] injectSession(ProceedingJoinPoint joinPoint, Session session) {
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