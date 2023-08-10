package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import static org.homeschoolpebt.app.utils.SubmissionUtilities.getSubmissionLanguage;

@Component
public class LastMinuteReminderMessage implements PebtMessage {
  Submission submission;

  public LastMinuteReminderMessage(Submission submission) {
    this.submission = submission;
  }

  @Override
  public Email renderEmail() {
    var applicantFullName = SubmissionUtilities.applicantFullName(submission);
    String subject;
    String body;
    if (getSubmissionLanguage(submission).equals("es")) {
      subject = "Tu solicitud para P-EBT 4.0 está incompleta (¡Las solicitudes cierran el 15 de agosto!)";
      body = """
        <html>
          <body>
            <p>Estimado %s,</p>
            <p>Gracias por tu interés en el programa P-EBT 4.0 de California. ¡Parece que comenzaste la solicitud, pero falta que la termines. Este es un recordatorio de que la solicitud se cerrará el martes 15 de agosto. Si aún deseas aplicar, visita la página <a href="https://www.getpebt.org">https://www.getpebt.org</a>.
            <p>Gracias por su interés en nuestro programa.</p>
            <p>- El Departamento de Servicios Sociales de California</p>
          </body>
        </html>
        """.formatted(StringUtils.escapeXml(applicantFullName));
    } else {
      subject = "Your PEBT 4.0 application is incomplete (Applications close on August 15th!)";
      body = """
        <html>
          <body>
            <p>Dear %s,</p>
            <p>Thank you for your interest in California's P-EBT 4.0 program. It looks like you started an application, but didn't finish it yet. This is a friendly reminder that the application will close on <strong>Tuesday, August 15th</strong>. If you still want to apply, go to <a href="https://www.getpebt.org">https://www.getpebt.org</a>.</p>
            <p>Thank you for your interest in our program.</p>
            <p>- California Department of Social Services</p>
          </body>
        </html>
        """.formatted(StringUtils.escapeXml(applicantFullName));
    }
    return new Email(subject, body);
  }

  @Override
  public Sms renderSms() {
    return null;
  }
}
