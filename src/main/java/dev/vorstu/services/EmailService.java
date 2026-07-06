package dev.vorstu.services;

import dev.vorstu.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${registration.confirm.base-url}")
    private String confirmBaseUrl;

    public void sendRegistrationLink(String email, String token) {
        String link = confirmBaseUrl + "?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Подтверждение регистрации");
        message.setText("Для завершения регистрации перейдите по ссылке:\n" + link
                + "\n\nСсылка действительна ограниченное время.");
        try {
            mailSender.send(message);
            log.info("Письмо с подтверждением отправлено на {}", email);
        } catch (Exception e) {
            log.error("Не удалось отправить письмо на {}: {}", email, e.getMessage());
            throw new BadRequestException("Не удалось отправить письмо подтверждения");
        }
    }
}
