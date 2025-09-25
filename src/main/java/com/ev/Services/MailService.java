package com.ev.Services;

import com.ev.Model.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MailService {

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();
    private final long OTP_VALIDITY_MS = 2 * 60 * 1000; // 2 minutes


    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void generateAndSendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        otpExpiry.put(email, System.currentTimeMillis() + OTP_VALIDITY_MS);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Verification Code");
        message.setText("Your OTP code is: " + otp + "\nIt expires in 2 minutes.");
        mailSender.send(message);
    }

    public boolean validateOtp(String email, String enteredOtp) {
        if (!otpStorage.containsKey(email)) return false;
        if (System.currentTimeMillis() > otpExpiry.getOrDefault(email, 0L)) return false;
        return otpStorage.get(email).equals(enteredOtp);
    }

    public void clearOtp(String email) {
        otpStorage.remove(email);
        otpExpiry.remove(email);
    }

    public void sendVillageConfirmationEmail(String toEmail, String villageName, Long villageId) {
        String subject = "Village Registration Request Received";

        String body = String.format(
                "Dear %s,\n\n" +
                        "Your village registration request has been submitted successfully.\n" +
                        "Request ID: %d\n\n" +
                        "Please wait for admin approval.\n\n" +
                        "Thank you,\nVillage Portal Team",
                villageName, villageId
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendVillageStatusUpdateEmail(String toEmail, String villageName, String status, LocalDateTime timestamp) {
        String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        String subject;
        String body;

        if ("APPROVED".equalsIgnoreCase(status)) {
            subject = "Village Registration Approved";
            body = String.format(
                    "Dear %s,\n\n" +
                            "Your village registration has been approved on %s.\n" +
                            "You can now log in using your registered email and password.\n\n" +
                            "Welcome aboard!\nVillage Portal Team",
                    villageName, formattedTime
            );
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            subject = "Village Registration Rejected";
            body = String.format(
                    "Dear %s,\n\n" +
                            "We regret to inform you that your village registration was rejected on %s.\n" +
                            "Please contact support if you believe this was a mistake.\n\n" +
                            "Regards,\nVillage Portal Team",
                    villageName, formattedTime
            );
        } else {
            subject = "Village Status Updated";
            body = String.format(
                    "Dear %s,\n\n" +
                            "Your village status has been updated to '%s' on %s.\n\n" +
                            "Regards,\nVillage Portal Team",
                    villageName, status, formattedTime
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendActivityCancellationEmail(String toEmail,
                                              String activityName,
                                              LocalDate bookingDate,
                                              String reason,
                                              String bookingStatus,
                                              Activity alternativeActivity,
                                              String villageEmail,
                                              Long bookingId) {

        String formattedDate = bookingDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String subject = "Update on Your Activity Booking";

        StringBuilder body = new StringBuilder();
        body.append("Dear user,\n\n")
                .append("Your booking for the activity '").append(activityName)
                .append("' on ").append(formattedDate).append(" has been updated.\n")
                .append("Reason: ").append(reason).append("\n\n");

        if ("confirmed".equalsIgnoreCase(bookingStatus)) {
            if (alternativeActivity != null && alternativeActivity.isAvailable()) {
                body.append("✅ You may use your ticket for the alternative activity '")
                        .append(alternativeActivity.getActivityName()).append("'.\n");
            } else {
                body.append("💸 If you had made a payment, a refund will be processed automatically.\n")
                        .append("For assistance, contact ").append(villageEmail)
                        .append(" with your booking ID: ").append(bookingId).append(".\n");
            }
        } else if ("pending".equalsIgnoreCase(bookingStatus)) {
            body.append("ℹ️ Since your booking was pending, no payment was processed.\n")
                    .append("You may book another available activity in the village.\n");
        }

        body.append("\nWe apologize for the inconvenience.\n\n")
                .append("Regards,\nVillage Portal Team");

        sendEmail(toEmail, subject, body.toString());
    }

    public void sendVillageStatusBasedEmail(String toEmail, String villageName, String status) {
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        String subject;
        String body;

        switch (status.toLowerCase()) {
            case "unavailable":
            case "deactivated":
                subject = "Village Status Changed – Bookings Cancelled";
                body = String.format(
                        "Dear %s,\n\n" +
                                "Your village '%s' has been marked as '%s' on %s.\n" +
                                "All confirmed bookings have been cancelled.\n" +
                                "If you had paid for any bookings, a refund will be processed automatically.\n\n" +
                                "We apologize for the inconvenience.\n\n" +
                                "Regards,\nVillage Portal Team",
                        villageName, villageName, status, formattedTime
                );
                break;

            default:
                subject = "Village Status Update";
                body = String.format(
                        "Dear %s,\n\n" +
                                "Your village status has been updated to '%s' on %s.\n\n" +
                                "Regards,\nVillage Portal Team",
                        villageName, status, formattedTime
                );
                break;
        }

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendActivityCancellationEmailDueToWeather(String toEmail,
                                              String activityName,
                                              LocalDate bookingDate,
                                              String reason,
                                              String bookingStatus,
                                              Activity alternativeActivity,
                                              String villageEmail,
                                              Long bookingId) {

        String formattedDate = bookingDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String subject = "Update on Your Activity Booking";

        StringBuilder body = new StringBuilder();
        body.append("Dear user,\n\n")
                .append("Your booking for the activity '").append(activityName)
                .append("' on ").append(formattedDate).append(" has been updated.\n")
                .append("Reason: ").append(reason).append("\n\n");

        if ("confirmed".equalsIgnoreCase(bookingStatus)) {
            if (alternativeActivity != null && "Available".equalsIgnoreCase(alternativeActivity.getActivityStatus())) {
                body.append("✅ Good news! You may use your ticket for the alternative activity '")
                        .append(alternativeActivity.getActivityName()).append("'.\n");
            } else {
                body.append("💸 If you had made a payment, a refund will be processed automatically.\n");
                body.append("For assistance, contact ").append(villageEmail)
                        .append(" with your booking ID: ").append(bookingId).append(".\n");
            }
        } else if ("pending".equalsIgnoreCase(bookingStatus)) {
            body.append("ℹ️ Since your booking was pending, no payment was processed.\n");
            body.append("You may book another available activity in the village.\n");
        }

        body.append("\nWe apologize for the inconvenience.\n\n")
                .append("Regards,\nVillage Portal Team");

        sendEmail(toEmail, subject, body.toString());
    }


    public void sendWeatherStatusChangeEmail(String toEmail, String activityName, String villageName, String newStatus, LocalDateTime timestamp) {
        String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        String subject;
        String body;

        if ("Unavailable".equalsIgnoreCase(newStatus)) {
            subject = "Activity Unavailable Due to Weather";
            body = String.format(
                    "Dear user,\n\n" +
                            "The activity '%s' in village '%s' has been marked as unavailable due to adverse weather conditions as of %s.\n" +
                            "All affected bookings will be cancelled and refunds processed automatically.\n\n" +
                            "We apologize for the inconvenience and appreciate your understanding.\n\n" +
                            "Regards,\nVillage Portal Team",
                    activityName, villageName, formattedTime
            );
        } else {
            subject = "Activity Available Again";
            body = String.format(
                    "Dear user,\n\n" +
                            "Good news! The activity '%s' in village '%s' is now available again as of %s.\n" +
                            "You may proceed to book or rebook the activity at your convenience.\n\n" +
                            "Stay safe and enjoy your experience!\n\n" +
                            "Regards,\nVillage Portal Team",
                    activityName, villageName, formattedTime
            );
        }
        sendEmail(toEmail, subject, body);
    }

    public void sendUserRemovalEmail(String toEmail, LocalDateTime timestamp) {
        String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        String subject = "Account Removal Notification";
        String body = String.format(
                "Dear user,\n\n" +
                        "Your account has been removed from the Village Portal as of %s due to activities that violated our community guidelines.\n" +
                        "This includes behavior deemed unfair, malicious, or harmful to other users or the integrity of the platform.\n\n" +
                        "If you believe this action was taken in error, you may contact our support team for further clarification.\n\n" +
                        "Regards,\nVillage Portal Team",
                formattedTime
        );

        sendEmail(toEmail, subject, body);
    }

    public void sendContactMessage(String senderName, String senderEmail, String subject, String messageBody) {
        String toEmail = "ethnicvillage2025@gmail.com";
        String fullSubject = "New Contact Message: " + subject;

        String body = String.format(
                "You have received a new message from Ethnic Village contact form:\n\n" +
                        "Name: %s\nEmail: %s\nSubject: %s\n\nMessage:\n%s",
                senderName, senderEmail, subject, messageBody
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(fullSubject);
        message.setText(body);
        mailSender.send(message);
    }



}
