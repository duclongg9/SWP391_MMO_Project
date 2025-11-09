/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package units;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author D E L L
 */
public class SendMail {

    public static void sendMail(String toEmail, String subject, String messageText) throws MessagingException, UnsupportedEncodingException {
        final String fromEmail = EmailConfig.getEmail(); // email của bạn
        final String password = EmailConfig.getPassword(); // password app hoặc real password nếu dùng smtp thường

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
        props.put("mail.smtp.port", "587"); // TLS Port
        props.put("mail.smtp.auth", "true"); // enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS
        props.put("mail.mime.charset", "UTF-8"); // ensure UTF-8 for all MIME parts

        // tạo phiên làm việc
        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        // tạo message
//        Message msg = new MimeMessage(session);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail, "Admin Material Management", "UTF-8"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
//        msg.setSubject(subject);
//        msg.setText(messageText);  // nếu dùng HTML thì dùng setContent(...)
        msg.setSubject(subject, "UTF-8");
        msg.setText(messageText, "UTF-8");

        // gửi
        Transport.send(msg);
    }

}
