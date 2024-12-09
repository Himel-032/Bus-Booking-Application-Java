package com.example.bookingbusticket.Model;

import android.os.AsyncTask;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSenderTask extends AsyncTask<Void, Void, Boolean> {
    private String senderEmail;
    private String appPassword;
    private String recipientEmail;
    private String emailMessage;

    public EmailSenderTask(String senderEmail, String appPassword, String recipientEmail, String emailMessage) {
        this.senderEmail = senderEmail;
        this.appPassword = appPassword;
        this.recipientEmail = recipientEmail;
        this.emailMessage = emailMessage;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // SMTP server configuration
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");  // SMTP server
        properties.put("mail.smtp.port", "587");            // Port
        properties.put("mail.smtp.auth", "true");           // Enable authentication
        properties.put("mail.smtp.starttls.enable", "true"); // Enable TLS

        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        try {
            // Create email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));        // Sender's email
            message.setRecipients(Message.RecipientType.TO,          // Recipient's email
                    InternetAddress.parse(recipientEmail));
            message.setSubject("Test Email");                        // Email subject
            message.setText(emailMessage);                           // Email body

            // Send email
            Transport.send(message);
            return true; // Success
        } catch (MessagingException e) {
            e.printStackTrace();
            return false; // Failure
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            System.out.println("Email sent successfully!");
        } else {
            System.out.println("Failed to send email.");
        }
    }
}
