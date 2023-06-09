package org.homeschoolpebt.app.submission.actions;

import com.mailgun.model.message.MessageResponse;
import com.twilio.rest.api.v2010.account.Message;
import formflow.library.data.Submission;
import formflow.library.email.MailgunEmailClient;
import org.homeschoolpebt.app.data.SentMessageRepositoryService;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.data.TransmissionRepositoryService;
import org.homeschoolpebt.app.submission.messages.TwilioSmsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandleApplicationSignedTest {
  @InjectMocks
  HandleApplicationSigned handleApplicationSigned;

  @Mock
  TransmissionRepositoryService transmissionRepositoryService;

  @Mock
  SentMessageRepositoryService sentMessageRepositoryService;

  @Mock
  MailgunEmailClient mailgunEmailClient;

  @Mock
  TwilioSmsClient twilioSmsClient;

  @Test
  void createsATransmission() {
    Submission submission = Submission.builder().flow("pebt").inputData(Map.ofEntries(
      Map.entry("firstName", "T. Est"),
      Map.entry("lastName", "Mc TestUser"),
      Map.entry("email", "test@example.com"),
      Map.entry("phoneNumber", "15105551234")
    )).build();

    Transmission transmission = Transmission.fromSubmission(submission);
    transmission.setConfirmationNumber("1000100");

    var mockMessageResponse = MessageResponse.builder().id("id").message("message").build();
    when(transmissionRepositoryService.createTransmissionRecord(submission)).thenReturn(transmission);
    when(mailgunEmailClient.sendEmail(any(), any(), any())).thenReturn(mockMessageResponse);
    when(twilioSmsClient.sendMessage(any(), any())).thenReturn(mock(Message.class));

    handleApplicationSigned.run(submission);

    verify(transmissionRepositoryService, times(1)).createTransmissionRecord(submission);
  }

  @Test
  void doesNothingIfTheTransmissionAlreadyExists() {
    Submission submission = Submission.builder().flow("pebt").inputData(Map.ofEntries(
      Map.entry("firstName", "T. Est"),
      Map.entry("lastName", "Mc TestUser"),
      Map.entry("email", "test@example.com"),
      Map.entry("phoneNumber", "15105551234")
    )).build();

    when(transmissionRepositoryService.transmissionExists(submission)).thenReturn(true);

    handleApplicationSigned.run(submission);

    verify(transmissionRepositoryService, times(0)).createTransmissionRecord(submission);
  }
}
