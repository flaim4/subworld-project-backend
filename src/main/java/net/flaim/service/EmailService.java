package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.dto.auth.MyCodeRequest;
import net.flaim.model.EmailVerification;
import net.flaim.model.User;
import net.flaim.repository.EmailRepository;
import net.flaim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public BaseResponse<Boolean> sendCodeEmail(String email, int code) throws MessagingException {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return BaseResponse.error(false);

        String content = buildVerificationEmailContent(user.get().getUsername(), code);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Подтверждение email адреса");
        helper.setText(content, true);

        EmailVerification emailVerification = new EmailVerification();

        emailVerification.setCode(code);
        emailVerification.setEmail(email);

        emailRepository.save(emailVerification);

        mailSender.send(message);
        return BaseResponse.success(true);
    }

    private String buildVerificationEmailContent(String username, int code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body>
                <div class="container">
                    <h2>Подтверждение email адреса</h2>
                    <p>Здравствуйте, <strong>%s</strong>!</p>
                    <p>Для завершения регистрации подтвердите ваш email адрес.</p>
                    <div class="code">%s</div>
                    <p>Код действителен в течение 15 минут.</p>
                </div>
            </body>
            </html>
            """.formatted(username, code);
    }

    public int generateVerificationCode() {
        SecureRandom secureRandom = new SecureRandom();
        return 100000 + secureRandom.nextInt(900000);
    }

    public BaseResponse<Boolean> myCode(MyCodeRequest myCodeRequest) {
        try {
            Optional<EmailVerification> emailVerification = emailRepository.findByEmail(myCodeRequest.getEmail());

            if (emailVerification.isEmpty()) {
                return BaseResponse.success(false);
            }

            EmailVerification verification = emailVerification.get();

            if (verification.getAttempts() >= 3) {
                emailRepository.delete(verification);
                sendCodeEmail(myCodeRequest.getEmail(), generateVerificationCode());
                return BaseResponse.success("Too many attempts, new code sent", false);
            }

            if (myCodeRequest.getCode() != verification.getCode()) {
                verification.setAttempts(verification.getAttempts() + 1);
                emailRepository.save(verification);
                return BaseResponse.success(false);
            }

            Optional<User> user = userRepository.findByEmail(myCodeRequest.getEmail());

            if (user.isEmpty()) {
                return BaseResponse.success("User not found", false);
            }

            User userToUpdate = user.get();
            userToUpdate.setVerifyEmail(true);
            userRepository.save(userToUpdate);
            emailRepository.delete(verification);

            return BaseResponse.success("Email has been successfully confirmed", true);

        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Verification error:", false);
        }
    }

}