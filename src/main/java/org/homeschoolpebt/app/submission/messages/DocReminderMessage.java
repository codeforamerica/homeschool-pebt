package org.homeschoolpebt.app.submission.messages;

import formflow.library.data.Submission;
import org.homeschoolpebt.app.data.Transmission;
import org.homeschoolpebt.app.utils.SubmissionUtilities;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import static org.homeschoolpebt.app.utils.SubmissionUtilities.getSubmissionLanguage;

@Component
public class DocReminderMessage implements PebtMessage {
  Submission submission;
  Transmission transmission;

  public DocReminderMessage(Submission submission, Transmission transmission) {
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
      subject = "Documentos necesarios para la solicitud P-EBT 4.0";
      body = """
        <html>
          <body>
            <p>Estimado %s,</p>
            <p>Gracias por iniciar su solicitud para recibir las prestaciones P-EBT. En este momento le recordamos que suba los documentos para su solicitud.\s
            Necesitará un comprobante de ingresos, una identificación de estudiante y documentación de la escuela virtual para cada estudiante, según corresponda.</p>
            <p>Puede encontrar el enlace aquí: https://getpebt.org/docs</p>
            <p>Su número de solicitud es %s.</p>
            <p>Si necesita alimentos ahora, puede ponerse en contacto con su banco de alimentos local en https://www.cafoodbanks.org/find-food. También puede solicitar CalFresh en https://www.getcalfresh.org.</p>
            <p>- Departamento de Servicios Sociales de California</p>
          </body>
        </html>
        """.formatted(StringUtils.escapeXml(applicantFullName), confirmationNumber);
    } else {
      subject = "Documents Needed for P-EBT 4.0 Application";
      body = """
        <html>
          <body>
            <p>Dear %s,</p>
            <p>Thank you for beginning the application for P-EBT benefits. This is a reminder to upload the documents for your application.\s
            You will need proof of income, a student  ID and virtual school documentation for each student, if applicable.</p>
            <p>You can find the link here: https://www.getpebt.org/docs</p>
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
        Gracias por iniciar la solicitud de prestaciones P-EBT. Le recordamos subir los documentos de su solicitud. Tendrá que subir comprobante de identidad de cada estudiante, comprobante de ingresos y documentación de la escuela virtual de cada estudiante, según corresponda.\s
        Su número de solicitud es %s y puede encontrar el enlace aquí: https://www.getpebt.org/docs
        """.formatted(confirmationNumber);
    } else {
      body = """
        Thank you for beginning the application for P-EBT benefits. This is a reminder to upload the documents for your application. You will need to upload proof of identity for each student, proof of income, and virtual school documentation for each student, if applicable.\s
        Your application number is %s and you can find the link here: https://www.getpebt.org/docs
        """.formatted(confirmationNumber);
    }
    return new Sms(body);
  }
}
