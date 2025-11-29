package net.flaim.service;

import lombok.RequiredArgsConstructor;
import net.flaim.dto.BaseResponse;
import net.flaim.model.User;
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

}