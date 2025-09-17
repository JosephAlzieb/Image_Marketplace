package com.marketplace.service;

import com.marketplace.model.entity.AuctionBid;
import com.marketplace.model.entity.Image;
import com.marketplace.model.entity.Transaction;
import com.marketplace.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@marketplace.com}")
    private String fromEmail;

    @Value("${marketplace.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.name:Digital Marketplace}")
    private String appName;

    /**
     * Send welcome email to new user
     */
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("loginUrl", frontendUrl + "/login");

            String htmlContent = templateEngine.process("emails/welcome", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Welcome to " + appName + "!",
                    htmlContent
            );

            logger.info("Welcome email sent to user: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send welcome email to user {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send seller welcome email
     */
    @Async
    public void sendSellerWelcomeEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("dashboardUrl", frontendUrl + "/seller/dashboard");

            String htmlContent = templateEngine.process("emails/seller-welcome", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Welcome to " + appName + " Seller Program!",
                    htmlContent
            );

            logger.info("Seller welcome email sent to user: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send seller welcome email to user {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send purchase confirmation email to buyer
     */
    @Async
    public void sendPurchaseConfirmationEmail(User buyer, Transaction transaction) {
        try {
            Context context = new Context();
            context.setVariable("buyer", buyer);
            context.setVariable("transaction", transaction);
            context.setVariable("image", transaction.getImage());
            context.setVariable("appName", appName);
            context.setVariable("downloadUrl", frontendUrl + "/downloads/" + transaction.getImage().getId());

            String htmlContent = templateEngine.process("emails/purchase-confirmation", context);

            sendHtmlEmail(
                    buyer.getEmail(),
                    "Purchase Confirmation - " + transaction.getImage().getTitle(),
                    htmlContent
            );

            logger.info("Purchase confirmation email sent to buyer: {}", buyer.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send purchase confirmation email to buyer {}: {}",
                    buyer.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send sale notification email to seller
     */
    @Async
    public void sendSaleNotificationEmail(User seller, Transaction transaction) {
        try {
            Context context = new Context();
            context.setVariable("seller", seller);
            context.setVariable("transaction", transaction);
            context.setVariable("image", transaction.getImage());
            context.setVariable("buyer", transaction.getBuyer());
            context.setVariable("appName", appName);
            context.setVariable("salesUrl", frontendUrl + "/seller/sales");

            String htmlContent = templateEngine.process("emails/sale-notification", context);

            sendHtmlEmail(
                    seller.getEmail(),
                    "Sale Notification - " + transaction.getImage().getTitle(),
                    htmlContent
            );

            logger.info("Sale notification email sent to seller: {}", seller.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send sale notification email to seller {}: {}",
                    seller.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send auction winner email
     */
    @Async
    public void sendAuctionWinnerEmail(User winner, Image image, AuctionBid winningBid) {
        try {
            Context context = new Context();
            context.setVariable("winner", winner);
            context.setVariable("image", image);
            context.setVariable("bid", winningBid);
            context.setVariable("appName", appName);
            context.setVariable("checkoutUrl", frontendUrl + "/checkout/auction/" + image.getId());

            String htmlContent = templateEngine.process("emails/auction-winner", context);

            sendHtmlEmail(
                    winner.getEmail(),
                    "Congratulations! You won the auction for " + image.getTitle(),
                    htmlContent
            );

            logger.info("Auction winner email sent to user: {}", winner.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send auction winner email to user {}: {}",
                    winner.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send auction lost email
     */
    @Async
    public void sendAuctionLostEmail(User bidder, Image image, AuctionBid winningBid, String reason) {
        try {
            Context context = new Context();
            context.setVariable("bidder", bidder);
            context.setVariable("image", image);
            context.setVariable("winningBid", winningBid);
            context.setVariable("reason", reason);
            context.setVariable("appName", appName);
            context.setVariable("auctionsUrl", frontendUrl + "/auctions");

            String htmlContent = templateEngine.process("emails/auction-lost", context);

            sendHtmlEmail(
                    bidder.getEmail(),
                    "Auction Result - " + image.getTitle(),
                    htmlContent
            );

            logger.info("Auction lost email sent to user: {}", bidder.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send auction lost email to user {}: {}",
                    bidder.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send refund confirmation email
     */
    @Async
    public void sendRefundConfirmationEmail(User buyer, Transaction transaction, BigDecimal refundAmount) {
        try {
            Context context = new Context();
            context.setVariable("buyer", buyer);
            context.setVariable("transaction", transaction);
            context.setVariable("refundAmount", refundAmount);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("emails/refund-confirmation", context);

            sendHtmlEmail(
                    buyer.getEmail(),
                    "Refund Processed - " + transaction.getImage().getTitle(),
                    htmlContent
            );

            logger.info("Refund confirmation email sent to buyer: {}", buyer.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send refund confirmation email to buyer {}: {}",
                    buyer.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send account suspension email
     */
    @Async
    public void sendAccountSuspensionEmail(User user, String reason) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("reason", reason);
            context.setVariable("appName", appName);
            context.setVariable("supportUrl", frontendUrl + "/support");

            String htmlContent = templateEngine.process("emails/account-suspension", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Account Suspension - " + appName,
                    htmlContent
            );

            logger.info("Account suspension email sent to user: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send account suspension email to user {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send account reactivation email
     */
    @Async
    public void sendAccountReactivationEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("loginUrl", frontendUrl + "/login");

            String htmlContent = templateEngine.process("emails/account-reactivation", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Account Reactivated - " + appName,
                    htmlContent
            );

            logger.info("Account reactivation email sent to user: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send account reactivation email to user {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send auction no bids email to seller
     */
    @Async
    public void sendAuctionNoBidsEmail(User seller, Image image) {
        try {
            Context context = new Context();
            context.setVariable("seller", seller);
            context.setVariable("image", image);
            context.setVariable("appName", appName);
            context.setVariable("relistUrl", frontendUrl + "/seller/images/" + image.getId() + "/relist");

            String htmlContent = templateEngine.process("emails/auction-no-bids", context);

            sendHtmlEmail(
                    seller.getEmail(),
                    "Auction Ended - No Bids Received",
                    htmlContent
            );

            logger.info("Auction no bids email sent to seller: {}", seller.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send auction no bids email to seller {}: {}",
                    seller.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send auction reserve not met email
     */
    @Async
    public void sendAuctionReserveNotMetEmail(User seller, Image image, AuctionBid highestBid) {
        try {
            Context context = new Context();
            context.setVariable("seller", seller);
            context.setVariable("image", image);
            context.setVariable("highestBid", highestBid);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("emails/auction-reserve-not-met", context);

            sendHtmlEmail(
                    seller.getEmail(),
                    "Auction Ended - Reserve Price Not Met",
                    htmlContent
            );

            logger.info("Auction reserve not met email sent to seller: {}", seller.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send auction reserve not met email to seller {}: {}",
                    seller.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send auction error email
     */
    @Async
    public void sendAuctionErrorEmail(User user, Image image, String error) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Auction Processing Error - " + image.getTitle());
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "We encountered an error while processing the auction for '%s'.\n\n" +
                            "Error: %s\n\n" +
                            "Our support team has been notified and will contact you shortly.\n\n" +
                            "Best regards,\n%s Team",
                    user.getFullName(), image.getTitle(), error, appName
            ));

            mailSender.send(message);

            logger.info("Auction error email sent to user: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send auction error email to user {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send refund notification to seller
     */
    @Async
    public void sendRefundNotificationEmail(User seller, Transaction transaction, BigDecimal refundAmount) {
        try {
            Context context = new Context();
            context.setVariable("seller", seller);
            context.setVariable("transaction", transaction);
            context.setVariable("refundAmount", refundAmount);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("emails/refund-notification", context);

            sendHtmlEmail(
                    seller.getEmail(),
                    "Refund Issued - " + transaction.getImage().getTitle(),
                    htmlContent
            );

            logger.info("Refund notification email sent to seller: {}", seller.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send refund notification email to seller {}: {}",
                    seller.getEmail(), e.getMessage(), e);
        }
    }

    // Private helper methods

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            // Fallback to simple text email
            sendSimpleEmail(to, subject, htmlContent.replaceAll("<[^>]*>", ""));
        }
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

        } catch (Exception e) {
            logger.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
        }
    }
}