package com.marketplace.mail;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;

@Service
public class MailService implements JavaMailSender {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Override
    public MimeMessage createMimeMessage() {
        logger.info("Creating new MimeMessage");
        return new MockMimeMessage();
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        logger.info("Creating MimeMessage from InputStream");
        return new MockMimeMessage();
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        logger.info("=== SENDING {} MIME MESSAGE(S) ===", mimeMessages.length);

        for (int i = 0; i < mimeMessages.length; i++) {
            MockMimeMessage mockMessage = (MockMimeMessage) mimeMessages[i];
            logger.info("--- MIME MESSAGE {} ---", i + 1);
            logger.info("From: {}", mockMessage.getFromAddress());
            logger.info("To: {}", Arrays.toString(mockMessage.getToAddresses()));
            logger.info("Subject: {}", mockMessage.getSubject());
            logger.info("Content Type: {}", mockMessage.getContentType());
            logger.info("Content: {}", mockMessage.getContent());
            logger.info("--- END MESSAGE {} ---", i + 1);
        }

        logger.info("=== ALL MIME MESSAGES LOGGED ===");
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        logger.info("=== SENDING {} SIMPLE MESSAGE(S) ===", simpleMessages.length);

        for (int i = 0; i < simpleMessages.length; i++) {
            SimpleMailMessage message = simpleMessages[i];
            logger.info("--- SIMPLE MESSAGE {} ---", i + 1);
            logger.info("From: {}", message.getFrom());
            logger.info("To: {}", Arrays.toString(message.getTo()));
            logger.info("Subject: {}", message.getSubject());
            logger.info("Text: {}", message.getText());
            if (message.getCc() != null) {
                logger.info("CC: {}", Arrays.toString(message.getCc()));
            }
            if (message.getBcc() != null) {
                logger.info("BCC: {}", Arrays.toString(message.getBcc()));
            }
            logger.info("--- END MESSAGE {} ---", i + 1);
        }

        logger.info("=== ALL SIMPLE MESSAGES LOGGED ===");
    }

    // Mock MimeMessage implementation for logging
    private static class MockMimeMessage extends MimeMessage {
        private String fromAddress;
        private String[] toAddresses;
        private String subject;
        private String content;
        private String contentType = "text/plain";

        public MockMimeMessage() {
            super((jakarta.mail.Session) null);
        }

        public void setFromAddress(String from) {
            this.fromAddress = from;
        }

        public String getFromAddress() {
            return fromAddress;
        }

        public void setToAddresses(String[] to) {
            this.toAddresses = to;
        }

        public String[] getToAddresses() {
            return toAddresses;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getSubject() {
            return subject;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
