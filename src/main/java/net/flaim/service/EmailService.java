package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.*;
import net.flaim.model.*;
import net.flaim.repository.EmailRepository;
import net.flaim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final SkinService skinService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public BaseResponse<String> sendCodeEmail(String email, int code) throws MessagingException {
        BaseResponse<String> validationResult = validateEmailOperation(email, EmailOperationType.VERIFICATION);
        if (!validationResult.isSuccess()) return validationResult;

        User user = userRepository.findByEmail(email).orElseThrow();

        Optional<EmailVerification> lastVerification = emailRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (lastVerification.isPresent()) {
            EmailVerification last = lastVerification.get();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoMinutesAgo = now.minusMinutes(2);
            if (last.getCreatedAt().isAfter(twoMinutesAgo)) return BaseResponse.error("You've made too many requests.");
        }

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setEmail(email);
        emailVerification.setCode(code);
        emailVerification.setAttempts(0);
        emailRepository.save(emailVerification);

        sendVerificationEmail(user.getUsername(), email, code);
        return BaseResponse.success("We have sent the code to your email address.");
    }

    @Transactional
    public BaseResponse<String> verifyCode(MyCodeRequest myCodeRequest) {
        try {
            Optional<EmailVerification> emailVerificationOpt = emailRepository.findByEmail(myCodeRequest.getEmail());

            if (emailVerificationOpt.isEmpty()) return BaseResponse.error("There is no user with this email address");

            EmailVerification verification = emailVerificationOpt.get();

            VerificationResult verificationResult = validateVerificationCode(verification, myCodeRequest.getCode());
            if (!verificationResult.isValid()) return BaseResponse.error(verificationResult.getErrorMessage());

            User user = userRepository.findByEmail(myCodeRequest.getEmail()).orElseThrow();
            user.setVerifyEmail(true);
            userRepository.save(user);

            emailRepository.delete(verification);
            skinService.createDefault(user);

            return BaseResponse.success("Email has been successfully confirmed");

        } catch (Exception e) {
            return BaseResponse.error("Verification failed");
        }
    }

    @Transactional
    public BaseResponse<String> sendPasswordResetCode(String email) throws MessagingException {
        BaseResponse<String> validationResult = validateEmailOperation(email, EmailOperationType.PASSWORD_RESET);
        if (!validationResult.isSuccess()) return validationResult;

        User user = userRepository.findByEmail(email).orElseThrow();

        Optional<EmailVerification> lastResetCode = emailRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (lastResetCode.isPresent()) {
            EmailVerification last = lastResetCode.get();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoMinutesAgo = now.minusMinutes(2);
            if (last.getCreatedAt().isAfter(twoMinutesAgo)) return BaseResponse.error("You've made too many requests.");
        }

        int resetCode = generateVerificationCode();

        EmailVerification resetVerification = new EmailVerification();
        resetVerification.setEmail(email);
        resetVerification.setCode(resetCode);
        resetVerification.setAttempts(0);
        emailRepository.save(resetVerification);

        sendPasswordResetEmail(user.getUsername(), email, resetCode);
        return BaseResponse.success("Password reset code sent to your email");
    }

    @Transactional
    public BaseResponse<String> resetPassword(ResetPasswordRequest request) {
        try {
            Optional<EmailVerification> resetVerificationOpt = emailRepository.findByEmail(request.getEmail());

            if (resetVerificationOpt.isEmpty()) return BaseResponse.error("No reset code found for this email");

            EmailVerification verification = resetVerificationOpt.get();

            int code;
            try {
                code = Integer.parseInt(request.getCode());
            } catch (NumberFormatException e) {
                return BaseResponse.error("Code must be 6 digits");
            }

            VerificationResult verificationResult = validateResetCode(verification, code);
            if (!verificationResult.isValid()) return BaseResponse.error(verificationResult.getErrorMessage());

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);

            emailRepository.delete(verification);
            return BaseResponse.success("Password has been successfully reset");

        } catch (Exception e) {
            return BaseResponse.error("Failed to reset password");
        }
    }

    public int generateVerificationCode() {
        SecureRandom secureRandom = new SecureRandom();
        return 100000 + secureRandom.nextInt(900000);
    }

    private BaseResponse<String> validateEmailOperation(String email, EmailOperationType operationType) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return BaseResponse.error("There is no user with this email address");

        User user = userOpt.get();

        switch (operationType) {
            case VERIFICATION:
                if (user.isVerifyEmail()) return BaseResponse.error("Email already verified");
                break;
            case PASSWORD_RESET:
                if (!user.isVerifyEmail()) return BaseResponse.error("Email not verified");
                break;
        }

        return BaseResponse.success("");
    }

    private VerificationResult validateVerificationCode(EmailVerification verification, int code) {
        if (verification.getAttempts() >= 3) {
            emailRepository.delete(verification);
            return VerificationResult.failure("You've made too many requests.");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailRepository.delete(verification);
            return VerificationResult.failure("Code has expired");
        }

        if (code != verification.getCode()) {
            verification.setAttempts(verification.getAttempts() + 1);
            emailRepository.save(verification);
            return VerificationResult.failure("Incorrect code");
        }

        return VerificationResult.success();
    }

    private VerificationResult validateResetCode(EmailVerification verification, int code) {
        if (verification.getAttempts() >= 3) {
            emailRepository.delete(verification);
            return VerificationResult.failure("Too many attempts. Please request a new code.");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailRepository.delete(verification);
            return VerificationResult.failure("Code has expired. Please request a new one.");
        }

        if (code != verification.getCode()) {
            verification.setAttempts(verification.getAttempts() + 1);
            emailRepository.save(verification);
            return VerificationResult.failure("Incorrect code");
        }

        return VerificationResult.success();
    }

    private void sendVerificationEmail(String username, String email, int code) throws MessagingException {
        String content = buildVerificationEmailContent(username, code);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Email Verification");
        helper.setText(content, true);
        mailSender.send(message);
    }

    private void sendPasswordResetEmail(String username, String email, int code) throws MessagingException {
        String content = buildPasswordResetEmailContent(username, code);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Password Reset Code");
        helper.setText(content, true);
        mailSender.send(message);
    }

    private String buildVerificationEmailContent(String username, int code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .code { font-size: 24px; font-weight: bold; color: #333; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Email Verification</h2>
                    <p>Hello, <strong>%s</strong>!</p>
                    <p>Please use the code below to verify your email address:</p>
                    <div class="code">%06d</div>
                    <p>This code is valid for 15 minutes.</p>
                </div>
            </body>
            </html>
            """.formatted(username, code);
    }

    private String buildPasswordResetEmailContent(String username, int code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .code { font-size: 24px; font-weight: bold; color: #333; margin: 20px 0; }
                    .warning { color: #dc3545; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Password Reset Request</h2>
                    <p>Hello, <strong>%s</strong>!</p>
                    <p>You have requested to reset your password. Use the code below:</p>
                    <div class="code">%06d</div>
                    <p class="warning">This code is valid for 15 minutes.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                </div>
            </body>
            </html>
            """.formatted(username, code);
    }
}