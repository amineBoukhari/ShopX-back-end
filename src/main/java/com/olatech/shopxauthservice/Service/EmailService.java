package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.StoreInvitation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JWTService jwtService;

    public void sendInvitationEmail(StoreInvitation invitation) throws MessagingException {
        // Ajoutez ce code temporairement dans votre EmailService ou dans un contrôleur de test
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            log.info("Looking for templates in classpath");
            Resource[] resources = resolver.getResources("classpath*:templates/**/*.html");
            for (Resource resource : resources) {
                System.out.println("Found template: " + resource.getURL());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String token = generateInvitationToken(invitation);
        String acceptUrl = "https://front-core-service-389976410269.europe-west1.run.app/invitations/accept?token=" + token;
        String rejectUrl = "https://front-core-service-389976410269.europe-west1.run.app/invitations/reject?token=" + token;

        Context context = new Context();
        context.setVariable("storeName", invitation.getStore().getName());
        context.setVariable("role", invitation.getRole().toString());
        context.setVariable("inviterName", invitation.getInviter().getUsername());
        context.setVariable("acceptUrl", acceptUrl);
        context.setVariable("rejectUrl", rejectUrl);
        context.setVariable("expiryDate", invitation.getExpiresAt());

        String emailContent = templateEngine.process("invitation-template", context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(invitation.getEmail());
        helper.setSubject("Invitation à rejoindre " + invitation.getStore().getName());
        helper.setText(emailContent, true);

        mailSender.send(mimeMessage);
    }

    private String generateInvitationToken(StoreInvitation invitation) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("invitationId", invitation.getId());
        claims.put("email", invitation.getEmail());
        claims.put("storeId", invitation.getStore().getId());

        // Token valide pendant 7 jours (même durée que l'invitation)
        return jwtService.generateTokenWithClaims(claims, 7 * 24 * 60 * 60 * 1000);
    }

    public void sendInvitationAcceptedNotification(StoreInvitation invitation) throws MessagingException {
        // Email de notification à l'invitant lorsque l'invitation est acceptée
        Context context = new Context();
        context.setVariable("storeName", invitation.getStore().getName());
        context.setVariable("role", invitation.getRole().toString());
        context.setVariable("invitedEmail", invitation.getEmail());

        String emailContent = templateEngine.process("invitation-accepted-template", context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(invitation.getInviter().getEmail());
        helper.setSubject("Invitation acceptée pour " + invitation.getStore().getName());
        helper.setText(emailContent, true);

        mailSender.send(mimeMessage);
    }
}