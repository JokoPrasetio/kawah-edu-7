package org.acme;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    public void sendEmailWithAttachment(String recipient, String subject, String body, String attachmentPath) throws IOException {
        File attachment = new File(attachmentPath);
        if (!attachment.exists()) {
            throw new IllegalArgumentException("File attachment tidak ditemukan: " + attachmentPath);
        }

        try {
            mailer.send(Mail.withHtml(recipient, subject, body)
                    .addAttachment(attachment.getName(), attachment, Files.probeContentType(attachment.toPath())));
        } catch (IOException e) {
            // menangani pengecualian IOException di sini
            e.printStackTrace();
        }
    }
}