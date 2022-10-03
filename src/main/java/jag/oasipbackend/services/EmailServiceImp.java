package jag.oasipbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImp implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("nonreply@intproj.ssi4");
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        mailSender.send(mailMessage);
    }
}
