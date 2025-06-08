package com.egrub.scanner.service;

import com.egrub.scanner.model.AnomalyData;
import com.egrub.scanner.model.ReportData;
import com.egrub.scanner.utils.MailHelperUtils;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TrackOpens;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailsResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
public class EmailService {

    private static final String MJ_APIKEY_PUBLIC = "demo";
    private static final String MJ_APIKEY_PRIVATE = "demo";
    private final MailjetClient client;

    public EmailService() {
        ClientOptions options = ClientOptions.builder()
                .apiKey(MJ_APIKEY_PUBLIC)
                .apiSecretKey(MJ_APIKEY_PRIVATE)
                .build();
        this.client = new MailjetClient(options);
    }

    public void alert() {
        try {

            List<AnomalyData> anomalies = Arrays.asList(
                    AnomalyData.builder()
                            .instrumentCode("ABC")
                            .timeStamp("9:20")
                            .close(99d)
                            .build(),
                    AnomalyData.builder()
                            .instrumentCode("DEF")
                            .timeStamp("9:70")
                            .close(99d)
                            .build(),
                    AnomalyData.builder()
                            .instrumentCode("GHI")
                            .timeStamp("9:50")
                            .close(99d)
                            .build()
            );

            ReportData reportData = new ReportData(
                    "Anomaly Report",
                    "Anomaly report",
                    anomalies,
                    anomalies.size()
            );

            // Generate HTML from the POJO
            String htmlContent = MailHelperUtils.generateHtmlFromPojo(reportData);

            // Send email with the generated HTML and an attachment
            SendEmailsResponse response = sendEmailWithAttachment(
                    "raviraj636@gmail.com",
                    "Anomalies",
                    "raviraj636@gmail.com",
                    "Ravi",
                    "Anomalies Report",
                    htmlContent,
                    "Please find below the anomalies",
                    "/path/to/report.pdf"
            );

        } catch (MailjetException | IOException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Send email with attachment using TransactionalEmail (Recommended approach)
     */
    private SendEmailsResponse sendEmailWithAttachment(
            String fromEmail,
            String fromName,
            String toEmail,
            String toName,
            String subject,
            String htmlContent,
            String textContent,
            String attachmentPath) throws MailjetException, IOException {

        TransactionalEmail message = TransactionalEmail
                .builder()
                .to(List.of(new SendContact(toEmail, toName),
                        new SendContact("metkarchetan@gmail.com", "Chetan Metkar")))
                .from(new SendContact(fromEmail, fromName))
                .htmlPart(htmlContent)
                .textPart(textContent != null ? textContent : "")
                .subject(subject)
                .trackOpens(TrackOpens.ENABLED)
                .customID("email-" + System.currentTimeMillis())
                .build();

        SendEmailsRequest request = SendEmailsRequest
                .builder()
                .message(message)
                .build();

        return request.sendWith(client);
    }
}
