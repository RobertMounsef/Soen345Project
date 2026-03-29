package com.ticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
            @Value("${app.mail.from:noreply@ticket.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendReservationConfirmation(String toEmail, String userName, String eventTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail.trim());
        message.setSubject("Reservation Confirmed – " + eventTitle);
        message.setText(
                "Hi " + userName + ",\n\n" +
                        "Your reservation for \"" + eventTitle + "\" has been confirmed!\n\n" +
                        "If you need to cancel, you can do so through the app at any time.\n\n" +
                        "Thank you for using our platform.");
        mailSender.send(message);
    }

    public void sendReservationCancellation(String toEmail, String userName, String eventTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail.trim());
        message.setSubject("Reservation Cancelled – " + eventTitle);
        message.setText(
                "Hi " + userName + ",\n\n" +
                        "Your reservation for \"" + eventTitle + "\" has been cancelled.\n\n" +
                        "If this was a mistake, please re-book through the app.\n\n" +
                        "Thank you for using our platform.");
        mailSender.send(message);
    }
}
