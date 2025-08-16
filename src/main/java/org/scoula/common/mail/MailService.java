package org.scoula.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:finpick2025@gmail.com}")
    private String from;

    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("[FinPick] 이메일 인증 코드");
            msg.setText("""
                    안녕하세요, FinPick 입니다.

                    아래 인증 코드를 5분 이내에 입력해주세요.

                    인증코드: %s

                    만약 본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.
                    """.formatted(code));
            mailSender.send(msg);
            log.info("📧 인증코드 메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("📧 인증코드 메일 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메일 발송 중 오류가 발생했습니다.");
        }
    }
}
