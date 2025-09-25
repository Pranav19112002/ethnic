package com.ev.Controller;

import com.ev.Services.ActivityBookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentRetrieveParams;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Scanner;

@RestController
@RequestMapping("/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired
    private final ActivityBookingService activityBookingService;

    public StripeWebhookController(ActivityBookingService activityBookingService) {
        this.activityBookingService = activityBookingService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        System.out.println("📩 [WEBHOOK] Stripe webhook received.");

        String payload;
        String sigHeader = request.getHeader("Stripe-Signature");

        // Step 1: Read the request body (payload)
        try (Scanner s = new Scanner(request.getInputStream()).useDelimiter("\\A")) {
            payload = s.hasNext() ? s.next() : "";
            System.out.println("📦 [WEBHOOK] Payload read successfully.");
        } catch (IOException e) {
            System.out.println("❌ [WEBHOOK] Failed to read payload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Failed to read payload");
        }

        // Step 2: Verify the signature using your webhook secret
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println("🔐 [WEBHOOK] Signature verified.");
        } catch (SignatureVerificationException e) {
            System.out.println("❌ [WEBHOOK] Signature verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("⚠️ Invalid signature");
        }

        System.out.println("📣 [WEBHOOK] Event type: " + event.getType());
        // Step 3: Only act if the event is "checkout.session.completed"
        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            if (deserializer.getObject().isPresent()) {
                Session session = (Session) deserializer.getObject().get();

                String bookingId = session.getClientReferenceId(); // we passed this when creating session
                String paymentIntentId = session.getPaymentIntent();

                System.out.println("📌 [WEBHOOK] Booking ID: " + bookingId);
                System.out.println("💳 [WEBHOOK] PaymentIntent ID: " + paymentIntentId);

                try {
                    // Step 4: Get PaymentIntent using paymentIntentId
                    PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                    System.out.println("✅ [WEBHOOK] PaymentIntent retrieved.");

                    // Step 5: Get the latest charge ID
                    String chargeId = paymentIntent.getLatestCharge();
                    System.out.println("🔄 [WEBHOOK] Charge ID: " + chargeId);
                    System.out.println("🔍 Checking chargeId: " + chargeId);

                    if (chargeId != null) {
                        // Step 6: Retrieve Charge using chargeId
                        Charge charge = Charge.retrieve(chargeId);

                        Long amountReceived = charge.getAmount(); // in cents
                        String currency = charge.getCurrency();   // e.g., "inr"

                        String paymentStatus = charge.getStatus(); // "succeeded", "pending", etc.
                        String receiptUrl = charge.getReceiptUrl(); // downloadable receipt
                        String paymentMethod = charge.getPaymentMethodDetails().getType(); // "card", "upi", etc.

                        System.out.println("💰 [WEBHOOK] Status: " + paymentStatus);
                        System.out.println("🧾 [WEBHOOK] Receipt URL: " + receiptUrl);
                        System.out.println("💳 [WEBHOOK] Payment Method: " + paymentMethod);

                        System.out.println("📞 Calling markAsPaid for bookingId: " + bookingId);
                        // Step 7: Save these details to your DB
                        activityBookingService.markAsPaid(
                                Long.parseLong(bookingId),
                                paymentIntentId,
                                paymentStatus,
                                paymentMethod,
                                receiptUrl,
                                amountReceived,
                                currency
                        );
                        System.out.println("✅ markAsPaid completed for bookingId: " + bookingId);
                        System.out.println("✅ [WEBHOOK] Booking marked as paid.");
                    } else {
                        System.out.println("⚠️ [WEBHOOK] No charge ID found in PaymentIntent.");
                    }
                } catch (Exception e) {
                    System.out.println("❌ [WEBHOOK] Error while updating booking: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("❌ Failed to update booking with payment info");
                }

            } else {
                System.out.println("❌ [WEBHOOK] Could not deserialize session.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Could not deserialize session");
            }
        }

        return ResponseEntity.ok("✅ Webhook received");
    }
}
