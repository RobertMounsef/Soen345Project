package com.ticket.unit;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import com.ticket.service.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmsService")
class SmsServiceTest {

    /** Twilio Account SID format: AC + 32 characters (test placeholder). */
    private static final String TEST_ACCOUNT_SID = "ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final String TEST_TOKEN = "test-auth-token";
    private static final String TEST_FROM = "+15551110000";
    private static final String TEST_TO = "+15552220000";
    private static final String TEST_MESSAGING_SID = "MGbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";

    @Nested
    @DisplayName("when SMS is disabled or misconfigured")
    class Inactive {

        @Test
        @DisplayName("does nothing when app.sms.enabled is false")
        void smsDisabled() {
            SmsService svc = new SmsService(false, TEST_ACCOUNT_SID, TEST_TOKEN, TEST_FROM, "");
            assertThatCode(() -> svc.sendReservationConfirmation(TEST_TO, "Alice", "Jazz Night"))
                    .doesNotThrowAnyException();
            assertThatCode(() -> svc.sendReservationCancellation(TEST_TO, "Alice", "Jazz Night"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("does nothing when account SID is not an AC sid")
        void invalidAccountSid() {
            SmsService svc = new SmsService(true, "MGwrongsidhere", TEST_TOKEN, TEST_FROM, "");
            assertThatCode(() -> svc.sendReservationConfirmation(TEST_TO, "Alice", "Jazz Night"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("does nothing when enabled but from and messaging service are both blank")
        void noSendPath() {
            SmsService svc = new SmsService(true, TEST_ACCOUNT_SID, TEST_TOKEN, "", "");
            assertThatCode(() -> svc.sendReservationConfirmation(TEST_TO, "Alice", "Jazz Night"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("skips send when destination phone is blank")
        void blankDestination() {
            SmsService svc = new SmsService(true, TEST_ACCOUNT_SID, TEST_TOKEN, TEST_FROM, "");
            assertThatCode(() -> svc.sendReservationConfirmation("  ", "Alice", "Jazz Night"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("when SMS is active with from number")
    class ActiveFromNumber {

        @Test
        @DisplayName("sendReservationConfirmation calls Twilio Message with expected body")
        void confirmationBody() {
            MessageCreator creator = mock(MessageCreator.class);
            Message message = mock(Message.class);
            when(creator.create()).thenReturn(message);

            try (MockedStatic<Twilio> twilio = mockStatic(Twilio.class);
                    MockedStatic<Message> msg = mockStatic(Message.class)) {

                msg.when(
                                () ->
                                        Message.creator(
                                                any(PhoneNumber.class),
                                                any(PhoneNumber.class),
                                                any(String.class)))
                        .thenReturn(creator);

                SmsService svc = new SmsService(true, TEST_ACCOUNT_SID, TEST_TOKEN, TEST_FROM, "");
                svc.sendReservationConfirmation(TEST_TO, "Alice", "Jazz Night");

                twilio.verify(() -> Twilio.init(eq(TEST_ACCOUNT_SID), eq(TEST_TOKEN)));
                msg.verify(
                        () ->
                                Message.creator(
                                        argThat((PhoneNumber to) -> TEST_TO.equals(to.getEndpoint())),
                                        argThat((PhoneNumber from) -> TEST_FROM.equals(from.getEndpoint())),
                                        argThat(
                                                (String body) ->
                                                        body.contains("Alice")
                                                                && body.contains("Jazz Night")
                                                                && body.contains("confirmed"))));
                verify(creator, times(1)).create();
            }
        }

        @Test
        @DisplayName("sendReservationCancellation calls Twilio Message with expected body")
        void cancellationBody() {
            MessageCreator creator = mock(MessageCreator.class);
            Message message = mock(Message.class);
            when(creator.create()).thenReturn(message);

            try (MockedStatic<Twilio> twilio = mockStatic(Twilio.class);
                    MockedStatic<Message> msg = mockStatic(Message.class)) {

                msg.when(
                                () ->
                                        Message.creator(
                                                any(PhoneNumber.class),
                                                any(PhoneNumber.class),
                                                any(String.class)))
                        .thenReturn(creator);

                SmsService svc = new SmsService(true, TEST_ACCOUNT_SID, TEST_TOKEN, TEST_FROM, "");
                svc.sendReservationCancellation(TEST_TO, "Alice", "Jazz Night");

                twilio.verify(() -> Twilio.init(eq(TEST_ACCOUNT_SID), eq(TEST_TOKEN)));
                msg.verify(
                        () ->
                                Message.creator(
                                        argThat((PhoneNumber to) -> TEST_TO.equals(to.getEndpoint())),
                                        any(PhoneNumber.class),
                                        argThat(
                                                (String body) ->
                                                        body.contains("Alice")
                                                                && body.contains("Jazz Night")
                                                                && body.contains("cancelled"))));
                verify(creator, times(1)).create();
            }
        }
    }

    @Nested
    @DisplayName("when SMS is active with Messaging Service")
    class ActiveMessagingService {

        @Test
        @DisplayName("sendReservationConfirmation uses Messaging Service SID instead of From")
        void usesMessagingService() {
            MessageCreator creator = mock(MessageCreator.class);
            Message message = mock(Message.class);
            when(creator.create()).thenReturn(message);

            try (MockedStatic<Twilio> twilio = mockStatic(Twilio.class);
                    MockedStatic<Message> msg = mockStatic(Message.class)) {

                msg.when(
                                () ->
                                        Message.creator(
                                                any(PhoneNumber.class),
                                                eq(TEST_MESSAGING_SID),
                                                any(String.class)))
                        .thenReturn(creator);

                SmsService svc = new SmsService(true, TEST_ACCOUNT_SID, TEST_TOKEN, "", TEST_MESSAGING_SID);
                svc.sendReservationConfirmation(TEST_TO, "Bob", "Rock Show");

                twilio.verify(() -> Twilio.init(eq(TEST_ACCOUNT_SID), eq(TEST_TOKEN)));
                msg.verify(
                        () ->
                                Message.creator(
                                        argThat((PhoneNumber to) -> TEST_TO.equals(to.getEndpoint())),
                                        eq(TEST_MESSAGING_SID),
                                        argThat(
                                                (String body) ->
                                                        body.contains("Bob") && body.contains("Rock Show"))));
                verify(creator, times(1)).create();
            }
        }
    }
}
