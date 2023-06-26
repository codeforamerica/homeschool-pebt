package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import static org.homeschoolpebt.app.utils.SubmissionUtilities.getSubmissionLanguage;

@Component
public class ConfirmationMessage implements PebtMessage {
  Submission submission;
  Transmission transmission;

  public ConfirmationMessage(Submission submission, Transmission transmission) {
    this.submission = submission;
    this.transmission = transmission;
  }

  @Override
  public Email renderEmail() {
    var confirmationNumber = SubmissionUtilities.getFormattedConfirmationNumber(transmission.getConfirmationNumber());
    var applicantFullName = SubmissionUtilities.applicantFullName(submission);
    String subject;
    String body;
    if (getSubmissionLanguage(submission).equals("es")) {
      subject = "Solicitud presentada para P-EBT 4.0";
      body = """
        <html>
          <body>
            <p>Estimado %s,</p>
            <p>Gracias por enviar su solicitud de beneficios P-EBT para el año escolar 2022-2023.\s
            El Departamento de Servicios Sociales de California le responderá por teléfono o por correo en las próximas 2 a 4 semanas.</p>
            <p>Su número de solicitud es %s.</p>
            <p>Si necesita alimentos ahora, puede ponerse en contacto con su banco de alimentos local en https://www.cafoodbanks.org/find-food. También puede solicitar CalFresh en https://www.getcalfresh.org.</p>
            <p>- Departamento de Servicios Sociales de California</p>
          </body>
        </html>
        """.formatted(StringUtils.escapeXml(applicantFullName), confirmationNumber);
    } else {
      subject = "Application Submitted for P-EBT 4.0";
      body = """
        <html>
          <body>
            <p>Dear %s,</p>
            <p>Thank you for submitting your application for P-EBT benefits for the 2022-2023 school year.\s
            You should hear back from the California Department of Social Services by phone or mail in the next 2-4 weeks.</p>
            <p>Your application number is %s.</p>
            <p>If you need food now, you can contact your local food bank at https://www.cafoodbanks.org/find-food. You can also apply for CalFresh at https://www.getcalfresh.org.</p>
            <p>- California Department of Social Services</p>
          </body>
        </html>
        """.formatted(StringUtils.escapeXml(applicantFullName), confirmationNumber);
    }
    return new Email(subject, body);
  }

  @Override
  public Sms renderSms() {
    var confirmationNumber = SubmissionUtilities.getFormattedConfirmationNumber(transmission.getConfirmationNumber());
    String body;
    if (getSubmissionLanguage(submission).equals("es")) {
      body = """
        Gracias por enviar la solicitud de prestaciones P-EBT para el curso escolar 2022-2023. Le contestaremos en un plazo de 2 a 4 semanas. Su número de solicitud es %s.\s
        -CDSS (Departamento de Servicios Sociales de California)
        """.formatted(confirmationNumber);
    } else {
      body = """
        Thank you for submitting the application for P-EBT benefits for the 2022-2023 school year. You will hear back within 2-4 weeks. Your application number is %s.\s
        -CDSS (California Dept of Social Services)
        """.formatted(confirmationNumber);
    }
    return new Sms(body);
  }
}
