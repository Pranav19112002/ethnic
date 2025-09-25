package com.ev.Controller;

import com.ev.Exception.ActivityBookingException;
import com.ev.Model.*;
import com.ev.Repository.UserFeedBackRepository;
import com.ev.Services.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private VillageService villageService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityBookingService activityBookingService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private BookingsFeedbackService bookingsFeedbackService;

    @Autowired
    private NotificationService notificationService;


    @Autowired
    private UserFeedBackRepository userFeedBackRepository;


    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final Map<String, Long> lastResendTime = new ConcurrentHashMap<>();
    private final long RESEND_COOLDOWN_MS = 30 * 1000; // 30 seconds

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("step", "email");
        model.addAttribute("userDto", new UserDto());
        return "user-reg";
    }

    @PostMapping("/sendOtp")
    public String sendOtp(@Valid @ModelAttribute("userDto") UserDto userDto, BindingResult result, Model model) {
        System.out.println("Step: sendOtp - Received email: " + userDto.getUserEmail());
        if (result.hasErrors()) {
            System.out.println("Step: sendOtp - Validation failed");
            model.addAttribute("step", "email");
            return "user-reg";
        }

        String userEmail = userDto.getUserEmail();
        boolean isEmailTaken = userService.isEmailTaken(userEmail);
        if (isEmailTaken){
            model.addAttribute("error","Email already Exist");
            model.addAttribute("step","email");
            return "user-reg";
        }
        mailService.generateAndSendOtp(userEmail);
        System.out.println("Step: sendOtp - OTP sent successfully");

        model.addAttribute("step", "otp");
        model.addAttribute("userEmail", userEmail);
        return "user-reg";
    }

    @PostMapping("/verifyOtp")
    public String verifyOtp(@RequestParam String userEmail, @RequestParam String otp, Model model) {
        System.out.println("Step: verifyOtp - Verifying OTP for: " + userEmail);
        if (mailService.validateOtp(userEmail, otp)) {
            System.out.println("Step: verifyOtp - OTP verified successfully");
            mailService.clearOtp(userEmail);
            UserDto userDto = new UserDto();
            userDto.setUserEmail(userEmail);
            model.addAttribute("userDto", userDto);
            model.addAttribute("step", "form");
        } else {
            System.out.println("Step: verifyOtp - OTP verification failed");
            model.addAttribute("step", "otp");
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("error", "Invalid OTP");
        }
        return "user-reg";
    }

    @PostMapping("/resendOtp")
    public String resendOtp(@RequestParam String userEmail, Model model) {
        System.out.println("Step: resendOtp - Resend requested for: " + userEmail);
        long now = System.currentTimeMillis();
        long lastTime = lastResendTime.getOrDefault(userEmail, 0L);

        if (now - lastTime < RESEND_COOLDOWN_MS) {
            System.out.println("Step: resendOtp - Cooldown active, resend blocked");
            model.addAttribute("step", "otp");
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("error", "Please wait before resending OTP.");
            return "user-reg";
        }

        mailService.generateAndSendOtp(userEmail);
        lastResendTime.put(userEmail, now);
        System.out.println("Step: resendOtp - OTP resent successfully");

        model.addAttribute("step", "otp");
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("resent", true);
        return "user-reg";
    }

    @PostMapping("/saveUser")
    public String saveUser(@Valid @ModelAttribute("userDto") UserDto userDto, BindingResult result, Model model , HttpSession session) {
        System.out.println("Step: saveUser - Saving user: " + userDto.getUserEmail());
        if (result.hasErrors()) {
            System.out.println("Step: saveUser - Validation failed");
            model.addAttribute("step", "form");
            return "user-reg";
        }

        String status = userService.saveUser(userDto);
        if (!status.equals("success")) {
            System.out.println("Step: saveUser - Save failed: " + status);
            model.addAttribute("errorMsg", status);
            model.addAttribute("step", "form");
            return "user-reg";
        }
        User savedUser = userService.getUserByEmail(userDto.getUserEmail());
        session.setAttribute("loggedInUser", savedUser);
        System.out.println("Step: saveUser - User saved successfully");
        if (savedUser == null){
            return "redirect:/loginPage";
        }
        return "redirect:/";
    }

    @GetMapping("/bookingPage")
    public String showBookingForm(@RequestParam Long villageId,
                                  @RequestParam Long activityId,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }

        Village village = villageService.getVillageById(villageId);
        Activity activity = activityService.getActivityById(activityId);
        if (village == null || activity == null) {
            return "redirect:/activityDetailsError";
        }

        List<Activity> activitiesOfVillage = activityService.getApprovedActivitiesByVillageId(villageId);


        ActivityBookingDto bookingDto = (ActivityBookingDto) model.getAttribute("activityBookingDto");
        if (bookingDto == null) {
            bookingDto = new ActivityBookingDto();
            bookingDto.setVillageId(villageId);
            bookingDto.setActivityId(activityId);
            bookingDto.setUserId(user.getUserId());
        }

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("today", java.time.LocalDate.now());
        model.addAttribute("activitiesOfVillage", activitiesOfVillage);
        model.addAttribute("village", village);
        model.addAttribute("activity", activity);
        model.addAttribute("activityBookingDto", bookingDto);
        model.addAttribute("user", user);

        return "user-activity-booking";
    }

    @GetMapping("/profile")
    public String userProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }
        User fullUser = userService.getUserById(user.getUserId());

        UserDto userDto = new UserDto();
        userDto.setUserEmail(fullUser.getUserEmail());
        userDto.setUserPhone(fullUser.getUserPhone());
        userDto.setUserName(fullUser.getUserName());

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("userDto", userDto);
        model.addAttribute("user", fullUser);

        return "user-profile";
    }

    @PostMapping("/updateProfileImage")
    public String updateUserProfileImage(@ModelAttribute UserDto userDto, Model model) {
        MultipartFile profileImageFile = userDto.getProfilePic();
        Long userId = userDto.getUserId();

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            String status = userService.updateUserImage(profileImageFile, userId);
            if (!"success".equals(status)) {
                model.addAttribute("imageUpdateMsg", "Image upload failed.");
            } else {
                model.addAttribute("imageUpdateMsg", "Profile image updated successfully.");
                System.out.println("Image uploaded");
            }
        } else {
            model.addAttribute("imageUpdateMsg", "No file selected");
        }

        // Add updated village info back to the model
        User updatedUser = userService.getUserById(userId);
        model.addAttribute("user", updatedUser);
        model.addAttribute("userDto", new UserDto()); // reset DTO if needed

        return "user-profile"; // return the same page with messages
    }

    @PostMapping("/deleteProfileImage")
    public String deleteProfileImage(@RequestParam("userId") Long userId , Model model , RedirectAttributes redirectAttributes) {
        String imgDeleted = userService.deleteUserImage(userId);
        if (!imgDeleted.equalsIgnoreCase("success")) {
            redirectAttributes.addFlashAttribute("imageDeleteMsg", "Image Delete Failed");
            model.addAttribute("userDto" , userService.getUserById(userId));
            return "user-profile";
        } else {
            redirectAttributes.addFlashAttribute("imageDeleteMsg", "Image Deleted successfully");
            return "redirect:/user/profile";
        }
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute("userDto") @Valid UserDto userDto,
                                BindingResult result,
                                Model model , RedirectAttributes redirectAttributes) {

        System.out.println("📥 Received Update Request: " + userDto.getUserId());
        System.out.println("👤 Data: Name=" + userDto.getUserName() + ", Email=" + userDto.getUserEmail() + ", Phone=" + userDto.getUserPhone());

        if (result.hasErrors()) {
            System.out.println("❌ Validation Errors Found:");
            result.getFieldErrors().forEach(error ->
                    System.out.println("Field: " + error.getField() + " -> " + error.getDefaultMessage()));
            model.addAttribute("user", userService.getUserById(userDto.getUserId()));
            model.addAttribute("userDto", userDto);
            model.addAttribute("showEditForm", true);
            return "user-profile"; // your Thymeleaf view name
        }

        String profileUpdate = userService.updateUserProfile(userDto);
        System.out.println("🔁 Service returned: " + profileUpdate);
        switch (profileUpdate) {
            case "email-exists":
                redirectAttributes.addFlashAttribute("profileUpdateMsg", " Email ID already exists.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
                break;

            case "phone-exists":
                redirectAttributes.addFlashAttribute("profileUpdateMsg", " Contact number already exists.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
                break;

            case "no-user":
                redirectAttributes.addFlashAttribute("profileUpdateMsg", " No user found.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
                break;

            case "success":
                redirectAttributes.addFlashAttribute("profileUpdateMsg", " Profile updated successfully.");
                return "redirect:/user/profile";

            default:
                redirectAttributes.addFlashAttribute("profileUpdateMsg", " Update failed. Please try again.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@Valid @ModelAttribute("userDto") UserDto userDto , BindingResult result ,
                                Model model , RedirectAttributes redirectAttributes){

        System.out.println("📥 Received Update Request: " + userDto.getUserId());
        System.out.println("👤 Data: CurrentPassword=" + userDto.getUserPassword() + ", NewPassword=" + userDto.getNewPassword() + ", ReenteredPassword=" + userDto.getConfirmPassword());

        if (result.hasErrors()) {
            System.out.println("❌ Validation Errors Found:");
            result.getFieldErrors().forEach(error ->
                    System.out.println("Field: " + error.getField() + " -> " + error.getDefaultMessage()));
            model.addAttribute("user", userService.getUserById(userDto.getUserId()));
            model.addAttribute("userDto", userDto);
            model.addAttribute("showResetPasswordForm", true);
            return "user-profile"; // your Thymeleaf view name
        }

        String resetPassword = userService.resetPassword(userDto);
        System.out.println("🔁 Service returned: " + resetPassword);
        switch (resetPassword) {
            case "password-error":
                redirectAttributes.addFlashAttribute("resetPasswordMsg", "Wrong User Password. Please Try Again.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
                break;

            case "password-miss-match":
                redirectAttributes.addFlashAttribute("resetPasswordMsg", "New Passwords Doesn't Match. Please Try Again.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
                break;

            case "no-user":
                redirectAttributes.addFlashAttribute("resetPasswordMsg", "No user found.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
                break;

            case "success":
                redirectAttributes.addFlashAttribute("resetPasswordMsg", " Password updated successfully.");
                return "redirect:/user/profile";

            default:
                redirectAttributes.addFlashAttribute("resetPasswordMsg", "Update failed. Please try again.");
                redirectAttributes.addFlashAttribute("userDto", userDto);
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/confirm-booking")
    public String confirmBooking(@ModelAttribute ActivityBookingDto dto, RedirectAttributes redirectAttributes) {
        try {
            if (dto.getUserId() == null || dto.getVillageId() == null || dto.getActivityId() == null) {
                throw new ActivityBookingException("One or more required IDs are missing.");
            }
            ActivityBooking booking = activityBookingService.createBooking(dto);
            notificationService.notifyVillageOfNewBooking(booking);
            redirectAttributes.addFlashAttribute("success", "Booking created. Proceed to payment.");
            return "redirect:/user/post-booking?bookingId=" + booking.getBookingId();

        } catch (ActivityBookingException ex) {

            redirectAttributes.addFlashAttribute("ActivityBookingDto", dto);
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/user/bookingPage?villageId=" + dto.getVillageId() + "&activityId=" + dto.getActivityId();
        }
    }

    @GetMapping("/validate-and-cancel-if-overbooked")
    public ResponseEntity<?> validateAndCancel(@RequestParam Long bookingId) {
        Map<String, Object> result = activityBookingService.validateAndCancelIfOverbooked(bookingId);
        return ResponseEntity.ok(result);
    }



    @GetMapping("/post-booking")
    public String showBookingPage(@RequestParam Long bookingId,
                                  @RequestParam(required = false) String session_id,
                                  HttpSession session,
                                  Model model,
                                  HttpServletResponse response) {


        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/loginPage";

        ActivityBooking activityBooking = activityBookingService.getActivityBookingsById(bookingId);
        BookingsFeedBack bookingFeedback = activityBooking.getBookingFeedback();
        activityBooking.setHasFeedback(bookingFeedback != null);

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("activityBooking", activityBooking);
        model.addAttribute("bookingFeedback", bookingFeedback);
        model.addAttribute("user", user);

        return "user-booking-details";
    }

    @PostMapping("/create-activity-checkout-session")
    @ResponseBody
    public Map<String, Object> createActivityCheckoutSession(@RequestParam Long bookingId,
                                                             @RequestParam(required = false) String filterDate,
                                                             @RequestParam(required = false) String filterActivity,
                                                             @RequestParam(required = false) String filterVillage,
                                                             HttpSession session) {
        System.out.println("⚙️ [CREATE SESSION] Starting for bookingId: " + bookingId);
        Map<String, Object> response = new HashMap<>();

        ActivityBooking activityBooking = activityBookingService.getBookingsById(bookingId);
        if (activityBooking == null) {
            System.out.println("❌ [CREATE SESSION] Booking not found for ID: " + bookingId);
            throw new RuntimeException("Booking not found");
        }

        Stripe.apiKey = stripeSecretKey;

        try {
            long amount = (long) (activityBooking.getTotalAmount() * 100);
            System.out.println("💰 [CREATE SESSION] Amount in paisa: " + amount);
            System.out.println("📧 [CREATE SESSION] Customer email: " + activityBooking.getUser().getUserEmail());

            String successUrl;
                if (filterDate == null) filterDate = (String) session.getAttribute("filterDate");
                if (filterActivity == null) filterActivity = (String) session.getAttribute("filterActivity");
                if (filterVillage == null) filterVillage = (String) session.getAttribute("filterVillage");

                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://localhost:8081/user/bookingsHistory/current")

                        .queryParam("bookingId", bookingId)
                        .queryParam("session_id", "{CHECKOUT_SESSION_ID}");

                if (filterDate != null) builder.queryParam("filterDate", filterDate);
                if (filterActivity != null) builder.queryParam("filterActivity", filterActivity);
                if (filterVillage != null) builder.queryParam("filterVillage", filterVillage);

                successUrl = builder.build().toUriString();


            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl("http://localhost:8080/payment-cancel")
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setCustomerEmail(activityBooking.getUser().getUserEmail())
                    .setClientReferenceId(String.valueOf(activityBooking.getBookingId())) // 👈 Important for webhook
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount((long) (activityBooking.getTotalAmount() * 100))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Activity Booking #" + activityBooking.getBookingId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session stripeSession  = Session.create(params);
            response.put("id", stripeSession .getId());
            System.out.println("✅ [CREATE SESSION] Session created with ID: " + stripeSession .getId());
            System.out.println("🔗 [CREATE SESSION] Checkout URL: " + stripeSession .getUrl());

        } catch (StripeException e) {
            System.out.println("❌ [CREATE SESSION] Stripe error: " + e.getMessage());
            throw new RuntimeException("Stripe checkout error", e);
        }

        return response;
    }


    @GetMapping("/api/check-payment-status")
    @ResponseBody
    public Map<String, Object> checkPaymentStatus(@RequestParam Long bookingId,
                                                  @RequestParam String sessionId) {
        ActivityBooking booking = activityBookingService.getBookingsById(bookingId);
        boolean isPaid = booking != null && "succeeded".equalsIgnoreCase(booking.getPaymentStatus());

        Map<String, Object> response = new HashMap<>();
        response.put("paymentConfirmed", isPaid);
        return response;
    }


    @GetMapping("/download-ticket")
    public void downloadTicket(@RequestParam Long bookingId, HttpServletResponse response) throws Exception {
        ActivityBooking booking = activityBookingService.getActivityBookingsById(bookingId);
        User user = booking.getUser();
        Activity activity = booking.getActivity();
        Village village = booking.getVillage();

        Context context = new Context();
        context.setVariable("activityBooking", booking);
        context.setVariable("user", user);
        context.setVariable("activity", activity);
        context.setVariable("village", village);

        String html = templateEngine.process("ticket-template", context);
        ByteArrayOutputStream pdfStream = pdfGeneratorService.generatePdfFromHtml(html);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=ticket_" + bookingId + ".pdf");
        response.getOutputStream().write(pdfStream.toByteArray());
        response.getOutputStream().flush();
    }



    @GetMapping("/bookingsHistory/pending")
    public String userPendingBookings(@RequestParam(required = false) String filterDate,
                                      @RequestParam(required = false) String filterActivity,
                                      @RequestParam(required = false) String filterVillage,
                                      Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }

        List<ActivityBooking> pendingBookings = activityBookingService.getPendingBookingsByUserId(
                filterDate, filterActivity, filterVillage,user.getUserId()
        );

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        model.addAttribute("filterDate", filterDate);
        model.addAttribute("filterActivity", filterActivity);
        model.addAttribute("filterVillage", filterVillage);

        model.addAttribute("pendingBookings", pendingBookings);

        return "user-pending-bookings";
    }
    @GetMapping("/bookingsHistory/current")
    public String userCurrentBookings(@RequestParam(required = false) String filterDate,
                                      @RequestParam(required = false) String filterActivity,
                                      @RequestParam(required = false) String filterVillage,
                                      Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }
        // Get user's bookings
        List<ActivityBooking> upcomingBookings = activityBookingService.getUpcomingBookingsByUserId(
                filterDate, filterActivity, filterVillage,user.getUserId()
        );

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        model.addAttribute("filterDate", filterDate);
        model.addAttribute("filterActivity", filterActivity);
        model.addAttribute("filterVillage", filterVillage);
        model.addAttribute("upcomingBookings", upcomingBookings);

        return "user-current-bookings";
    }

    @GetMapping("/bookingsHistory/past")
    public String userPastBookings(@RequestParam(required = false) String filterDate,
                                      @RequestParam(required = false) String filterActivity,
                                      @RequestParam(required = false) String filterVillage,
                                      Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }
        // Get user's bookings
        List<ActivityBooking> endedBookings = activityBookingService.getEndedBookingsByUserId(
                filterDate, filterActivity, filterVillage,user.getUserId()
        );

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        model.addAttribute("filterDate", filterDate);
        model.addAttribute("filterActivity", filterActivity);
        model.addAttribute("filterVillage", filterVillage);
        model.addAttribute("endedBookings", endedBookings);

        return "user-past-bookings";
    }

    @GetMapping("/bookingsHistory/cancelled")
    public String userCancelledBookings(@RequestParam(required = false) String filterDate,
                                   @RequestParam(required = false) String filterActivity,
                                   @RequestParam(required = false) String filterVillage,
                                   Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }
        // Get user's bookings
        List<ActivityBooking> cancelledAndExpiredBookings = activityBookingService.getCancelledAndExpiredBookingsByUserId(
                filterDate, filterActivity, filterVillage,user.getUserId()
        );

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        model.addAttribute("filterDate", filterDate);
        model.addAttribute("filterActivity", filterActivity);
        model.addAttribute("filterVillage", filterVillage);
        model.addAttribute("cancelledAndExpiredBookings", cancelledAndExpiredBookings);

        return "user-cancelled-exp-bookings";
    }

    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam Long bookingId,
                                @RequestParam String cancelReason,
                                RedirectAttributes redirectAttrs) {
        boolean success = activityBookingService.cancelBooking(bookingId, cancelReason);

        if (success) {
            redirectAttrs.addFlashAttribute("cancelSuccess", true);
            notificationService.notifyVillageBookingCancelled(bookingId, cancelReason);
        } else {
            redirectAttrs.addFlashAttribute("cancelFailed", true);
        }

        return "redirect:/user/bookingsHistory/cancelled";
    }

    @PostMapping("/submit-bookings-feedback")
    public String submitFeedback(@RequestParam Long bookingId,
                                 @RequestParam int rating,
                                 @RequestParam String feedback,
                                 RedirectAttributes redirectAttributes) {
        bookingsFeedbackService.saveFeedback(bookingId, rating, feedback);
        redirectAttributes.addFlashAttribute("feedbackSuccess", true);
        return "redirect:/user/bookingsHistory/past";
    }


    @GetMapping("/paymentsHistory")
    public String userPayments(@RequestParam(required = false) String filterStatus,
                               @RequestParam(required = false) String filterMethod,
                               @RequestParam(required = false) String filterDate,
                                        Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }

        List<ActivityBooking> payments = activityBookingService.getPaymentsByUserId(
                filterDate, filterMethod, filterStatus,user.getUserId()
        );

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        model.addAttribute("filterDate", filterDate);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("filterMethod", filterMethod);
        model.addAttribute("payments", payments);

        return "user-payments";
    }

    @GetMapping("/notifications")
    public String viewUserInbox(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }

        List<UserInboxMessage> messages = notificationService.getAllMessagesForUser(user.getUserId());
        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();

        model.addAttribute("userInboxMessages", messages);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);

        return "user-messages";
    }

    @PostMapping("/inbox/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long id) {
        notificationService.markUserMessageAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 🔹 Mark all messages as read
    @PostMapping("/inbox/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllMessagesAsRead(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.badRequest().body("User not logged in");
        }

        notificationService.markAllUserMessagesAsRead(user.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/submit-feedback")
    @ResponseBody
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, String> payload, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (userFeedBackRepository.existsByUser(user)) return ResponseEntity.ok().build();

        UserFeedBack feedback = new UserFeedBack();
        feedback.setUser(user);
        feedback.setRating(Integer.parseInt(payload.get("rating")));
        feedback.setComment(payload.get("comment"));
        userFeedBackRepository.save(feedback);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/update-feedback")
    public String updateFeedback(@RequestParam int rating,
                                 @RequestParam String comment,
                                 @RequestParam Long userId,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(userId);

        UserFeedBack feedback = userFeedBackRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("No existing feedback to update"));

        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setSubmittedAt(LocalDateTime.now());

        userFeedBackRepository.save(feedback);

        redirectAttributes.addFlashAttribute("feedbackSuccess", "✅ Feedback updated successfully!");
        return "redirect:/user/feedback";
    }


    @GetMapping("/feedback")
    public String viewFeedback(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        UserFeedBack feedback = userFeedBackRepository.findByUser(user).orElse(null);

        int unreadCount = notificationService.getUnreadMessagesForUser(user.getUserId()).size();
        model.addAttribute("unreadCount",unreadCount);
        model.addAttribute("userFeedback", feedback);
        model.addAttribute("user", user);
        return "user-feedback";
    }

    @GetMapping("/logout")
    public String ViewLogoutPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/loginPage";
        }
        model.addAttribute("user", user);
        return "user-logout";
    }

    @PostMapping("/logoutNow")
    public String performLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/loginPage";
    }

}
