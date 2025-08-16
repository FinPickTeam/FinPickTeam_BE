package org.scoula.common.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Value("${spring.mail.host}") private String host;
    @Value("${spring.mail.port}") private int port;
    @Value("${spring.mail.username}") private String username;
    @Value("${spring.mail.password}") private String password;

    @Bean
    public org.springframework.mail.javamail.JavaMailSender mailSender() {
        JavaMailSenderImpl s = new JavaMailSenderImpl();
        s.setHost(host);
        s.setPort(port);
        s.setUsername(username);
        s.setPassword(password);

        Properties props = s.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.transport.protocol", "smtp");
        return s;
    }
}
