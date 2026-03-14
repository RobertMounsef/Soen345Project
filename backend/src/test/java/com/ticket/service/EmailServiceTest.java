package com.ticket.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    //sendReservationConfirmation

    @Nested
    @DisplayName("sendReservationConfirmation")
    class SendConfirmation {

        @Test
        @DisplayName("sends an email with the correct recipient")
        void correctRecipient() {
            emailService.sendReservationConfirmation("alice@test.com", "Alice", "Jazz Night");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getTo()).contains("alice@test.com");
        }

        @Test
        @DisplayName("subject contains the event title")
        void subjectContainsEventTitle() {
            emailService.sendReservationConfirmation("alice@test.com", "Alice", "Jazz Night");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getSubject()).contains("Jazz Night");
        }

        @Test
        @DisplayName("body greets user by name")
        void bodyContainsUserName() {
            emailService.sendReservationConfirmation("alice@test.com", "Alice", "Jazz Night");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getText()).contains("Alice");
        }

        @Test
        @DisplayName("body mentions the event title (edge case: long event name)")
        void bodyContainsEventTitle() {
            String longTitle = "A Very Long Event Title That Goes On And On";
            emailService.sendReservationConfirmation("alice@test.com", "Alice", longTitle);

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getText()).contains(longTitle);
        }

        @Test
        @DisplayName("calls mailSender.send exactly once")
        void sendsExactlyOnce() {
            emailService.sendReservationConfirmation("alice@test.com", "Alice", "Jazz Night");
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }
    }

    // sendReservationCancellation

    @Nested
    @DisplayName("sendReservationCancellation")
    class SendCancellation {

        @Test
        @DisplayName("sends an email with the correct recipient")
        void correctRecipient() {
            emailService.sendReservationCancellation("alice@test.com", "Alice", "Jazz Night");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getTo()).contains("alice@test.com");
        }

        @Test
        @DisplayName("subject indicates cancellation and includes event title")
        void subjectIndicatesCancellation() {
            emailService.sendReservationCancellation("alice@test.com", "Alice", "Jazz Night");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getSubject())
                    .contains("Cancelled")
                    .contains("Jazz Night");
        }

        @Test
        @DisplayName("body greets user by name")
        void bodyContainsUserName() {
            emailService.sendReservationCancellation("alice@test.com", "Alice", "Jazz Night");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getText()).contains("Alice");
        }

        @Test
        @DisplayName("calls mailSender.send exactly once")
        void sendsExactlyOnce() {
            emailService.sendReservationCancellation("alice@test.com", "Alice", "Jazz Night");
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }
    }
}
