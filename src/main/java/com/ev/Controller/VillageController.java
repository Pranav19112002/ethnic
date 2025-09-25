package com.ev.Controller;

import com.ev.Model.*;
import com.ev.Repository.UserFeedBackRepository;
import com.ev.Services.*;
import com.ev.ValidationGroups.EmailUpdateGroup;
import com.ev.ValidationGroups.NewEmailUpdateGroup;
import com.ev.ValidationGroups.PasswordUpdateGroup;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Controller
@RequestMapping("/village")
public class VillageController {

    @Autowired
    private VillageService villageService;

    @Autowired
    ActivityService activityService;

    @Autowired
    RoomService roomService;

    @Autowired
    TempVillageService tempVillageService;

    @Autowired
    MailService mailService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    ActivityBookingService activityBookingService;

    @Autowired
    VillageStayService villageStayService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    VillageDashboardServices villageDashboardServices;

    @Autowired
    UserFeedBackRepository userFeedBackRepository;


    private final Map<String, Long> lastResendTime = new ConcurrentHashMap<>();
    private final long RESEND_COOLDOWN_MS = 30 * 1000; // 30 seconds

    @GetMapping("/home")
    public String viewVillageHomePage(Model model, HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");

        if (village == null) {
            model.addAttribute("message", "No village found for this user.");
            return "redirect:/loginPage";
        }
        String confirmedBookings = villageDashboardServices.getFormattedConfirmedBookings(village.getVillageId());
        String cancelledBookings = villageDashboardServices.getFormattedCancelledBookings(village.getVillageId());
        String endedBookings = villageDashboardServices.getFormattedEndedBookings(village.getVillageId());
        String formattedRevenue = villageDashboardServices.getFormattedRevenue(village.getVillageId());
        String availableActivities = villageDashboardServices.getFormattedAvailableActivityCount(village.getVillageId());
        String activeStays = villageDashboardServices.getFormattedActiveVillageStayCount(village.getVillageId());
        String allActivities = villageDashboardServices.getFormattedAllActivitiesCount(village.getVillageId());
        String allStays = villageDashboardServices.getFormattedAllStays(village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("allStays", allStays);
        model.addAttribute("allActivities", allActivities);
        model.addAttribute("availableActivities", availableActivities);
        model.addAttribute("activeStays", activeStays);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("endedBookings", endedBookings);
        model.addAttribute("formattedRevenue", formattedRevenue);
        model.addAttribute("village", village);

        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);

        return "village-homepage";
    }


    @GetMapping("/register")
    public String viewVillageRegister(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");

        VillageDto villageDto = new VillageDto();

        String captcha = villageService.generateCaptcha();
        session.setAttribute("captchaCode", captcha);

        model.addAttribute("captchaCode", captcha);
        model.addAttribute("villageDto", villageDto);
        return "user-village-reg";
    }

    @PostMapping("/sendOtp")
    public String sendOtp(@Valid @ModelAttribute("villageDto") VillageDto villageDto,
                          BindingResult result,
                          @RequestParam("captchaInput") String captchaInput,
                          HttpSession session,
                          Model model) {
        System.out.println("Step: sendOtp - Received email: " + villageDto.getVillageEmail());
        System.out.println("Step: sendOtp - DTO: " + villageDto);

        String expectedCaptcha = (String) session.getAttribute("captchaCode");

        if (!captchaInput.equalsIgnoreCase(expectedCaptcha)) {
            model.addAttribute("errorMessage", "Invalid CAPTCHA.");
            model.addAttribute("captchaCode", villageService.generateCaptcha());
            session.setAttribute("captchaCode", model.getAttribute("captchaCode"));
            return "user-village-reg";
        }

        if(!villageDto.getNewPassword().equals(villageDto.getConfirmPassword())){
            model.asMap().remove("otpPhase");
            model.addAttribute("passwordError", "PASSWORDS Not Matching.");
            model.addAttribute("captchaCode", villageService.generateCaptcha());
            session.setAttribute("captchaCode", model.getAttribute("captchaCode"));
            return "user-village-reg";
        }

        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            model.asMap().remove("otpPhase");
            model.addAttribute("errorMessage", "Please correct the errors.");
            model.addAttribute("captchaCode", villageService.generateCaptcha());
            session.setAttribute("captchaCode", model.getAttribute("captchaCode"));
            return "user-village-reg";
        }

        if (villageDto.getOfficialDoc() != null && !villageDto.getOfficialDoc().isEmpty()) {
            String path = villageService.saveFile(villageDto.getOfficialDoc());
            villageDto.setOfficialDocPath(path);
        }

        if (villageDto.getOffStampOrSeal() != null && !villageDto.getOffStampOrSeal().isEmpty()) {
            String path = villageService.saveFile(villageDto.getOffStampOrSeal());
            villageDto.setOffStampOrSealPath(path);
        }

        if (villageDto.getVillagePhotos() != null && !villageDto.getVillagePhotos().isEmpty()) {
            List<String> paths = villageService.saveMultipleFiles(villageDto.getVillagePhotos());
            villageDto.setVillagePhotoPaths(paths);
        }

        session.setAttribute("lockedVillageDto", villageDto);
        session.setAttribute("villageEmailToVerify", villageDto.getVillageEmail());
        mailService.generateAndSendOtp(villageDto.getVillageEmail());

        model.addAttribute("otpPhase", true);
        model.addAttribute("villageEmail", villageDto.getVillageEmail());
        return "user-village-reg";
    }

    @PostMapping("/verifyOtp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            Model model, RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("villageEmailToVerify");
        VillageDto lockedDto = (VillageDto) session.getAttribute("lockedVillageDto");

        String docPath = lockedDto.getOfficialDocPath();
        String stampPath = lockedDto.getOffStampOrSealPath();
        List<String> photoPaths = lockedDto.getVillagePhotoPaths();

        lockedDto.setOfficialDoc(null); // Clear MultipartFile
        lockedDto.setOffStampOrSeal(null);
        lockedDto.setVillagePhotos(null);

        if (mailService.validateOtp(email, otp)) {
            mailService.clearOtp(email);
            String status = villageService.registerVillage(lockedDto);

            if (!"success".equals(status)) {
                model.addAttribute("errorMessage", status);
                model.addAttribute("villageDto", lockedDto);
                model.asMap().remove("otpPhase");
                String newCaptcha = villageService.generateCaptcha();
                model.addAttribute("captchaCode", newCaptcha);
                session.setAttribute("captchaCode", newCaptcha);
                model.addAttribute("registrationFailed", true);
                model.addAttribute("errorMessage", status);

                return "user-village-reg";
            }
            Village savedVillageRequest = villageService.getVillageByEmail(lockedDto.getVillageEmail());
            mailService.sendVillageConfirmationEmail(savedVillageRequest.getVillageEmail(),savedVillageRequest.getVillageName(),savedVillageRequest.getVillageId());
            notificationService.sendVillageNotificationToAdmin(savedVillageRequest.getVillageId(),"Village Registration Request");
            System.out.println("Village registration status: " + status);
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            return "redirect:/";

        } else {
            model.addAttribute("otpPhase", true);
            model.addAttribute("villageEmail", email);
            model.addAttribute("villageDto", lockedDto);
            model.addAttribute("errorMessage", "Invalid or expired OTP.");
            return "user-village-reg";
        }
    }


    @PostMapping("/resendOtp")
    public String resendOtp(@RequestParam("villageEmail") String email,
                            HttpSession session,
                            Model model) {

        if (email == null || email.isBlank()) {
            model.addAttribute("errorMessage", "Email is missing.");
            model.asMap().remove("otpPhase");
            model.addAttribute("villageDto", new VillageDto());
            String newCaptcha = villageService.generateCaptcha();
            model.addAttribute("captchaCode", newCaptcha);
            session.setAttribute("captchaCode", newCaptcha);
            return "user-village-reg";
        }

        mailService.generateAndSendOtp(email);
        session.setAttribute("villageEmailToVerify", email);

        VillageDto lockedDto = (VillageDto) session.getAttribute("lockedVillageDto");
        if (lockedDto != null) {
            model.addAttribute("villageDto", lockedDto);
        }

        model.addAttribute("otpPhase", true);
        model.addAttribute("villageEmail", email);
        model.addAttribute("message", "OTP resent successfully.");
        return "user-village-reg";
    }

    @GetMapping("/settings")
    public String viewSettingsPage(Model model, HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        VillageDto villageDto =  new VillageDto();
        villageDto.setVillageEmail(village.getVillageEmail());
        villageDto.setVillageId(village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("step", "email");
        model.addAttribute("village", village);
        model.addAttribute("villageDto", villageDto);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);

        return "village-settings";
    }
    @GetMapping("/settings/email")
    public String viewSettingsEmailPage(Model model, HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        VillageDto villageDto =  new VillageDto();
        villageDto.setVillageEmail(village.getVillageEmail());
        villageDto.setVillageId(village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();


        model.addAttribute("step", "email");
        model.addAttribute("village", village);
        model.addAttribute("villageDto", villageDto);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-settings";
    }

    @PostMapping("/sendUpdateOtp")
    public String sendOtp(@Validated(NewEmailUpdateGroup.class) @ModelAttribute("villageDto") VillageDto villageDto,
                          BindingResult result, Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) return "redirect:/loginPage";

        System.out.println("Step: sendOtp - Received new email: " + villageDto.getNewEmail());

        if (result.hasErrors()) {
            model.addAttribute("step", "email");
            model.addAttribute("village", village);
            return "village-settings";
        }

        String newEmail = villageDto.getNewEmail();
        mailService.generateAndSendOtp(newEmail);
        lastResendTime.put(newEmail, System.currentTimeMillis());

        model.addAttribute("step", "otp");
        model.addAttribute("villageEmail", newEmail);
        model.addAttribute("villageDto", villageDto);
        model.addAttribute("village", village);
        return "village-settings";
    }

    @PostMapping("/verifyUpdateOtp")
    public String verifyOtp(@RequestParam String villageEmail, @RequestParam String otp,
                            Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) return "redirect:/loginPage";

        System.out.println("Step: verifyOtp - Verifying OTP for new email: " + villageEmail);

        if (mailService.validateOtp(villageEmail, otp)) {
            mailService.clearOtp(villageEmail);

            VillageDto villageDto = new VillageDto();
            villageDto.setNewEmail(villageEmail);
            villageDto.setVillageId(village.getVillageId());

            model.addAttribute("villageDto", villageDto);
            model.addAttribute("step", "form");
        } else {
            model.addAttribute("step", "otp");
            model.addAttribute("villageEmail", villageEmail);
            model.addAttribute("error", "Invalid OTP");
        }

        model.addAttribute("village", village);
        return "village-settings";
    }


    @PostMapping("/resendUpdateOtp")
    public String resendUpdateOtp(@RequestParam String villageEmail, Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) return "redirect:/loginPage";

        long now = System.currentTimeMillis();
        long lastTime = lastResendTime.getOrDefault(villageEmail, 0L);

        if (now - lastTime < RESEND_COOLDOWN_MS) {
            model.addAttribute("step", "otp");
            model.addAttribute("villageEmail", villageEmail);
            model.addAttribute("error", "Please wait before resending OTP.");
            model.addAttribute("village", village);
            return "village-settings";
        }

        mailService.generateAndSendOtp(villageEmail);
        lastResendTime.put(villageEmail, now);

        model.addAttribute("step", "otp");
        model.addAttribute("villageEmail", villageEmail);
        model.addAttribute("resent", true);
        model.addAttribute("village", village);
        return "village-settings";
    }


    @PostMapping("/updateVillageEmail")
    public String saveUser(@Validated(NewEmailUpdateGroup.class) @ModelAttribute("villageDto") VillageDto villageDto,
                           BindingResult result, Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) return "redirect:/loginPage";

        System.out.println("Step: updateVillageEmail - New email: " + villageDto.getNewEmail());

        if (result.hasErrors()) {
            model.addAttribute("step", "form");
            model.addAttribute("village", village);
            return "village-settings";
        }

        boolean isEmailTaken = villageService.isEmailTaken(villageDto.getNewEmail());
        if (isEmailTaken) {
            model.addAttribute("errorMsg", "Email already exists");
            model.addAttribute("step", "form");
            model.addAttribute("village", village);
            return "village-settings";
        }

        villageDto.setVillageEmail(villageDto.getNewEmail()); // Ensure session update uses new email
        String status = villageService.updateVillageEmail(villageDto);
        if (!"success".equals(status)) {
            model.addAttribute("errorMsg", status);
            model.addAttribute("step", "form");
            model.addAttribute("village", village);
            return "village-settings";
        }

        Village savedVillage = villageService.getVillageByEmail(villageDto.getNewEmail());
        if (savedVillage == null) return "redirect:/loginPage";

        session.setAttribute("loggedInVillage", savedVillage);
        return "redirect:/village/villageDetails";
    }

    @GetMapping("/settings/password")
    public String switchToPassword(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) return "redirect:/loginPage";

        VillageDto villageDto = new VillageDto();
        villageDto.setVillageEmail(village.getVillageEmail());
        villageDto.setVillageId(village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();


        model.addAttribute("step", "password");
        model.addAttribute("village", village);
        model.addAttribute("villageDto", villageDto);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-settings";
    }


    @PostMapping("/updateVillagePassword")
    public String updatePassword(@Validated(PasswordUpdateGroup.class) @ModelAttribute("villageDto") VillageDto villageDto,
                                 BindingResult result, Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) return "redirect:/loginPage";

        if (!passwordEncoder.matches(villageDto.getCurrentPassword() , village.getVillagePassword())){
            model.addAttribute("passwordError", "Error Current Password.");
            model.addAttribute("step", "password");
            model.addAttribute("village", village);
            return "village-settings";
        }
        if (!villageDto.getNewPassword().equals(villageDto.getConfirmPassword())) {
            model.addAttribute("passwordError", "New password and confirmation do not match.");
            model.addAttribute("step", "password");
            model.addAttribute("village", village);
            return "village-settings";
        }

        String status = villageService.updatePassword(village.getVillageId() , villageDto.getNewPassword());
        if (!"success".equals(status)) {
            model.addAttribute("passwordError", "Update Failed");
            model.addAttribute("step", "password");
            model.addAttribute("village", village);
            return "village-settings";
        }

        model.addAttribute("successMsg", "Password updated successfully.");
        model.addAttribute("step", "email");
        model.addAttribute("village", village);
        return "village-settings";
    }

    @GetMapping("/villageDetails")
    public String viewVillageDetails(Model model , HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        VillageDto villageDto = new VillageDto();
        model.addAttribute("villageDto",villageDto);
        model.addAttribute("village",village);
        return "village-details";
    }

    @PostMapping("/updateProfileImage")
    public String updateVillageProfileImage(@ModelAttribute VillageDto villageDto, Model model) {
        MultipartFile profileImageFile = villageDto.getVillageProfileImageFile();
        Long villageId = villageDto.getVillageId();

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            String status = villageService.updateVillageProfileImage(profileImageFile, villageId);
            if (!"success".equals(status)) {
                model.addAttribute("error", "Image upload failed.");
            } else {
                model.addAttribute("success", "Profile image updated successfully.");
                System.out.println("Image uploaded");
            }
        } else {
            model.addAttribute("error", "No file selected.");
        }

        Village updatedVillage = villageService.getVillageById(villageId);
        model.addAttribute("village", updatedVillage);
        model.addAttribute("villageDto", new VillageDto());

        return "village-details";
    }

    @PostMapping("/addGalleryPhotos")
    public String addVillageGalleryPhotos(@RequestParam("villageId") Long villageId,
                                          @RequestParam("villageGalleryFiles") MultipartFile[] files,
                                          RedirectAttributes redirectAttributes) {

        List<MultipartFile> fileList = Arrays.asList(files);
        String result = villageService.addMoreVillageImages(villageId, fileList);

        if (result.startsWith("Success=")) {
            String message = result.substring("Success=".length());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } else if (result.startsWith("Error=")) {
            String message = result.substring("Error=".length());
            redirectAttributes.addFlashAttribute("errorMessage", message);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong. Please try again.");
        }

        return "redirect:/village/villageDetails?villageId=" + villageId;
    }


    @PostMapping("/updateVillageImage")
    public ResponseEntity<String> updateVillageImage(
            @RequestParam("villageId") Long villageId,
            @RequestParam("oldImageName") String oldImageName,
            @RequestParam("newImage") MultipartFile newImageFile) {

        try {
            boolean updated = villageService.updateVillageImage(villageId, oldImageName, newImageFile);

            if (updated) {
                return ResponseEntity.ok("Image updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update image.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the image.");
        }
    }

    @DeleteMapping("/deleteProfileImage")
    public ResponseEntity<Map<String, String>> deleteVillageProfileImage(@RequestParam("villageId") Long villageId) {
        Map<String, String> response = new HashMap<>();
        try {
            String result = villageService.deleteVillageProfileImage(villageId);

            if (result.startsWith("Success=")) {
                response.put("status", "success");
                response.put("message", result.substring(8));
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", result.substring(6));
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Unexpected server error. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/deleteVillageImage")
    @ResponseBody
    public Map<String, Object> deleteVillageImage(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String imageName = payload.get("imageName");
        Long villageId = Long.valueOf(payload.get("villageId"));

        boolean success = villageService.deleteImageFromVillage(villageId, imageName);
        response.put("success", success);
        response.put("message", success ? "Image deleted successfully!" : "Failed to delete image.");
        return response;
    }

    @PostMapping("/updateVillageCurrentStatus")
    public ResponseEntity<Map<String, Object>> updateVillageCurrentStatus(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long villageId = Long.parseLong(requestData.get("villageId"));
            String newStatus = requestData.get("newStatus");

            boolean success = villageService.updateVillageStatusAndCascade(villageId, newStatus);

            if (success) {
                response.put("success", true);
                response.put("message", "Village status updated and cascade completed.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Update failed. Village or activities not found.");
                return ResponseEntity.badRequest().body(response);
            }
        }  catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Internal error occurred.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @GetMapping("/notifications")
    public String viewAllMessages(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        List<VillageInboxMessage> allMessages = notificationService.getVillageMessagesByTimeStamp(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("villageMessages", allMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-notifications"; // Thymeleaf page
    }

    @PostMapping("/messages/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead(HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        notificationService.markAllVillageMessagesAsRead(village);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long id) {
        notificationService.markVillageMessageAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/editVillage")
    public String viewUpdateVillage(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        TempVillageDto tempVillageDto = new TempVillageDto();
        tempVillageDto.setVillageId(village.getVillageId());
        tempVillageDto.setVillageEmail(village.getVillageEmail());
        tempVillageDto.setVillageName(village.getVillageName());
        tempVillageDto.setVillageType(village.getVillageType());
        tempVillageDto.setVillageLocation(village.getVillageLocation());
        tempVillageDto.setLatitude(village.getLatitude());
        tempVillageDto.setLongitude(village.getLongitude());
        tempVillageDto.setVillageDescription(village.getVillageDescription());
        tempVillageDto.setContactPersonName(village.getContactPersonName());
        tempVillageDto.setContactEmail(village.getContactEmail());
        tempVillageDto.setContactNo(village.getContactNo());
        tempVillageDto.setAltContactNo(village.getAltContactNo());

        tempVillageDto.setVillageRegNumber(village.getVillageRegNumber());
        tempVillageDto.setVillageAuthorityName(village.getVillageAuthorityName());
        tempVillageDto.setLegalRepresentativeName(village.getVillageRepresentativeName());
        tempVillageDto.setRepresentativePosition(village.getRepresentativePosition());
        tempVillageDto.setOfficialIdNumber(village.getOfficialIdNumber());

        tempVillageDto.setBankName(village.getBankName());
        tempVillageDto.setBankAccountNumber(village.getBankAccountNumber());
        tempVillageDto.setTaxInfoNumber(village.getTin());


        String status = "Pending";
        boolean requestExits = tempVillageService.isRequestExists(village.getVillageId(),status);
        if (requestExits){
            model.addAttribute("requestMsg","Request Already in Process,Please Try Again Later.");
        }
        String captcha = villageService.generateCaptcha();
        session.setAttribute("captchaCode", captcha);

        model.addAttribute("captchaCode",captcha);
        model.addAttribute("tempVillageDto", tempVillageDto);
        return "village-details-edit";
    }

    @PostMapping("/sendUpdateVillageDetailsRequest")
    public String updateVillageDetails(
            @Valid @ModelAttribute("tempVillageDto") TempVillageDto tempVillageDto,
            BindingResult result,
            @RequestParam("captchaInput") String captchaInput,
            Model model,
            HttpSession session
    ) {
        Village loggedInVillage = (Village) session.getAttribute("loggedInVillage");

        if (loggedInVillage == null) {
            model.addAttribute("message", "Session expired. Please log in again.");
            return "redirect:/loginPage";
        }

        if (result.hasErrors()) {
            System.out.println("Validation errors:");
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));

            model.addAttribute("captchaCode", villageService.generateCaptcha());
            model.addAttribute("village", loggedInVillage);
            model.addAttribute("tempVillageDto", tempVillageDto); // re-bind form
            return "village-details-edit";
        }
        String expectedCaptcha = (String) session.getAttribute("captchaCode");

        if (!captchaInput.equalsIgnoreCase(expectedCaptcha)) {
            model.addAttribute("errorMessage", "Invalid CAPTCHA.");
            model.addAttribute("captchaCode", villageService.generateCaptcha());
            session.setAttribute("captchaCode", model.getAttribute("captchaCode"));
            model.addAttribute("village", loggedInVillage);
            model.addAttribute("tempVillageDto", tempVillageDto);
            return "village-details-edit";
        }

        try {
            String status = tempVillageService.addUpdateVillageRequest(tempVillageDto);

            if ("success".equals(status)) {
                TempVillage tempVillage = tempVillageService.getTempVillageById(tempVillageDto.getVillageId());
                notificationService.sendVillageNotificationToAdmin(tempVillage.getVillageId(),"Village Update Request");
                model.addAttribute("message", "Your update request has been sent for admin approval.");
                return "redirect:/village/editVillageRequests";
            } else {
                model.addAttribute("message", "Something went wrong. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "An error occurred while sending the request.");
        }

        model.addAttribute("tempVillageDto", tempVillageDto);
        model.addAttribute("village", loggedInVillage);
        return "village-details-edit";
    }

    @GetMapping("/editVillageRequests")
    public String viewUpdateVillageRequests(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        List<TempVillage> villageRequests = tempVillageService.getVillageUpdateRequests(village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("villageRequests",villageRequests);
        model.addAttribute("village",village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-edit-reqs";
    }

    @PostMapping("/deleteUpdateVillageRequest")
    public ResponseEntity<Map<String, Object>> deleteUpdateVillageRequest(@RequestParam("requestId") Long requestId) {
        Map<String, Object> response = new HashMap<>();
        try {

            boolean isDeleted = tempVillageService.deleteUpdateVillageRequestById(requestId);

            if (isDeleted) {
                response.put("status", "success");
                response.put("message", "Request deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Request not found or could not be deleted.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/logout")
    public String ViewLogoutPage(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        model.addAttribute("village", village);
        return "village-logout";
    }

    @PostMapping("/logoutNow")
    public String performLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/loginPage";
    }

    @GetMapping("/addActivity")
    public String newActivity(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        List<Activity> permanentActivities = activityService.searchActivitiesByType("permanent", village.getVillageId());
        ActivityDto activityDto = new ActivityDto();
        activityDto.setVillageId(village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();


        model.addAttribute("permanentActivities", permanentActivities);
        model.addAttribute("village", village);
        model.addAttribute("activityDto", activityDto);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-add-cultural";
    }

    @PostMapping("/addActivity")
    public String createActivity(@Valid @ModelAttribute("activityDto") ActivityDto activityDto, BindingResult result, Model model,RedirectAttributes redirectAttributes, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.toString()));
            model.addAttribute("village", village);
            return "village-add-cultural";
        }
        boolean isNameTaken = activityService.isActivityNameTaken(activityDto.getActivityName(),activityDto.getVillageId());
        if (isNameTaken) {
            model.addAttribute("village", village);
            model.addAttribute("errorMsg", "Activity Already Exist");
            return "village-add-cultural";
        }
        Activity savedActivity = activityService.saveActivity(activityDto);
        if (savedActivity == null) {
            model.addAttribute("village", village);
            model.addAttribute("errorMsg", "Activity Registration Failed");
            return "village-add-cultural";
        }
            notificationService.sendActivityNotificationToAdmin(savedActivity,"Activity Approval Request");
        redirectAttributes.addFlashAttribute("activityRequestSuccess", true);
        return "redirect:/village/showPendingActivities";
    }

    @GetMapping("/showPendingActivities")
    public String showPendingActivities(
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "allButton", required = false) String allButton,
            Model model, HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        List<Activity> activities;
        if ("All".equals(allButton)) {
            searchKeyword = null;
            activities = activityService.getAllPendingActivitiesByVillageId(village.getVillageId());
        } else if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            activities = activityService.searchPendingActivitiesByName(searchKeyword, village.getVillageId());
        } else {
            activities = activityService.getAllPendingActivitiesByVillageId(village.getVillageId());
        }

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("village", village);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("activities", activities);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-activities-pending";
    }

    @PostMapping("/deleteActivityReq")
    public ResponseEntity<Map<String, Object>> deleteActivityRequest(@RequestParam("activityId") Long activityId) {
        Map<String, Object> response = new HashMap<>();
        try {

            boolean isDeleted = activityService.deleteActivityById(activityId);

            if (isDeleted) {
                response.put("status", "success");
                response.put("message", "Activity deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Activity not found or could not be deleted.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/viewAllApprovedActivities")
    public String showActivities(@RequestParam(value = "selectedType", required = false) String selectedType,
                                    @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
                                    @RequestParam(value = "statusFilter", required = false) String statusFilter,
                                    Model model, HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        System.out.println("Received Parameters:");
        System.out.println("selectedType = " + selectedType);
        System.out.println("searchKeyword = " + searchKeyword);
        System.out.println("statusFilter = " + statusFilter);
        List<String> activityTypes = activityService.getAllActivityTypes();
        List<Activity> activities;
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            selectedType = null;
        }
        if (selectedType != null && !selectedType.equalsIgnoreCase("All") && !selectedType.isEmpty()) {
            searchKeyword = null;
        }
        activities = activityService.getAllFilteredActivitiesForVillageAdmin(searchKeyword,selectedType,statusFilter,village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("village", village);
        model.addAttribute("activityTypes", activityTypes);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("activities", activities);
        model.addAttribute("statusFilter",statusFilter);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-activities-approved";
    }

    @PostMapping("/updateActivityStatus")
    public ResponseEntity<Map<String, Object>> updateActivityStatus(@RequestBody Map<String, String> requestData) {
        try {
            Long activityId = Long.parseLong(requestData.get("activityId"));
            String newStatus = requestData.get("newStatus");

            boolean isUpdated = activityService.updateActivityStatus(activityId, newStatus);

            if (isUpdated) {
                return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Update failed!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update status"));
        }
    }

    @PostMapping("/updateActivityImage")
    public ResponseEntity<String> updateActivityImage(
            @RequestParam("newImage") MultipartFile newImage,
            @RequestParam("oldImageName") String oldImageName,
            @RequestParam("activityId") Long activityId) {
        try {
            boolean updated = activityService.updateActivityImage(newImage, oldImageName, activityId);
            if (updated) {
                return ResponseEntity.ok("Image updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update image");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    @PostMapping("/addMoreActivityImages")
    @ResponseBody
    public Map<String, Object> addActivityImages(@RequestParam("images") List<MultipartFile> images,
                                                 @RequestParam("activityId") Long activityId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = activityService.addMoreImagesToActivity(activityId, images);
            response.put("success", success);
            response.put("message", success ? "Images added successfully!" : "Cannot add images. Max limit (5) exceeded.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error while adding images.");
        }
        return response;
    }

    @PostMapping("/deleteActivityImage")
    @ResponseBody
    public Map<String, Object> deleteActivityImage(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String imageName = payload.get("imageName");
        Long activityId = Long.valueOf(payload.get("activityId"));

        boolean success = activityService.deleteImageFromActivity(activityId, imageName);
        response.put("success", success);
        response.put("message", success ? "Image deleted successfully!" : "Failed to delete image.");
        return response;
    }

    @GetMapping("/editActivity/{activityId}")
    public String editActivity(@PathVariable("activityId") Long activityId, Model model, HttpSession session ) {


        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        Activity activity = activityService.getActivityById(activityId);
        if (activity == null) {
            model.addAttribute("errorMsg", "Activity not found!");
            return "error-page"; // Handle activity not found error
        }

        List<Activity> permanentActivities = activityService.searchActivitiesByType("permanent", village.getVillageId());

        ActivityDto activityDto = new ActivityDto();
        activityDto.setVillageId(activity.getVillage().getVillageId());
        activityDto.setActivityId(activityId);
        activityDto.setActivityName(activity.getActivityName());
        activityDto.setActivityPlace(activity.getActivityPlace());
        activityDto.setDescription(activity.getDescription());
        activityDto.setActivityType(activity.getActivityType());
        activityDto.setDuration(activity.getDuration());
        activityDto.setPrice(activity.getPrice());
        activityDto.setEventDateTime(activity.getEventDateTime());
        activityDto.setNoOfPeopleAllowedForDateOnly(activity.getNoOfPeopleAllowedForDateOnly());
        if (activity.getAlternativeActivity() != null) {
            activityDto.setAlternativeActivityId(activity.getAlternativeActivity().getActivityId());
        } else {
            activityDto.setAlternativeActivityId(null); // optional, depending on how your form handles it
        }
        activityDto.setStartDate(activity.getStartDate());
        activityDto.setEndDate(activity.getEndDate());
        activityDto.setAvailableDays(activity.getAvailableDays());
        activityDto.setTimeSlots(activity.getTimeSlots());
        activityDto.setNoOfPeopleAllowedForSloted(activity.getNoOfPeopleAllowedForSloted());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();


        model.addAttribute("activityDto",activityDto);
        model.addAttribute("permanentActivities", permanentActivities);
        model.addAttribute("village", village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-edit-cultural";
    }

    @PostMapping("/updateActivity")
    public String updateActivity(@Valid ActivityDto activityDto,BindingResult result,Model model,HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (result.hasErrors()) {
            System.out.println("Validation errors:");
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage())); //
            model.addAttribute("village",village);
            return "village-edit-cultural";
        }
        String status = activityService.updateActivity(activityDto);
        if (!status.equals("success")){
            model.addAttribute("village",village);
            model.addAttribute("errorMsg",status);
            return "village-edit-cultural";
        }

        Activity activity = activityService.getActivityById(activityDto.getActivityId());
        if (activity != null) {
            String requestStatus = activity.getRequestStatus();
            if ("Rejected".equalsIgnoreCase(requestStatus)) {
                return "redirect:/village/viewAllRejectedActivities";
            } else if ("Approved".equalsIgnoreCase(requestStatus)) {
                return "redirect:/village/viewAllApprovedActivities";
            } else {
                return "redirect:/village/showPendingActivities";
            }
        }

        model.addAttribute("errorMsg", "Activity not found or status unavailable.");
        return "village-edit-cultural";
    }

    @GetMapping("/viewAllRejectedActivities")
    public String showRejectedActivities(@RequestParam(value = "selectedType", required = false) String selectedType,
                                 @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
                                 Model model, HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        System.out.println("Received Parameters:");
        System.out.println("selectedType = " + selectedType);
        System.out.println("searchKeyword = " + searchKeyword);
        List<String> activityTypes = activityService.getAllActivityTypes();
        List<Activity> activities;
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            selectedType = null;
        }
        if (selectedType != null && !selectedType.equalsIgnoreCase("All") && !selectedType.isEmpty()) {
            searchKeyword = null;
        }
        activities = activityService.getAllFilteredRejectedActivitiesForVillageAdmin(searchKeyword,selectedType,village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("village", village);
        model.addAttribute("activityTypes", activityTypes);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("activities", activities);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-activities-rejected";
    }

    @PostMapping("/updateActivityRequestStatus")
    public ResponseEntity<Map<String, Object>> updateActivityRequestStatus(@RequestBody Map<String, String> requestData) {
        try {
            System.out.println("[DEBUG] Incoming Request Data: " + requestData);

            Long activityId = Long.parseLong(requestData.get("activityId"));
            Long villageId = Long.parseLong(requestData.get("villageId"));
            String newStatus = requestData.get("newStatus");

            System.out.println("[DEBUG] Parsed activityId: " + activityId);
            System.out.println("[DEBUG] Parsed villageId: " + villageId);
            System.out.println("[DEBUG] New status: " + newStatus);

            boolean isUpdated = activityService.updateRequestStatus(activityId, newStatus, villageId);

            if (isUpdated) {
                System.out.println("[DEBUG] Status updated successfully.");
                return ResponseEntity.ok(Map.of("success", true, "message", "Status updated successfully"));
            } else {
                System.out.println("[DEBUG] Update failed - Activity not found or village mismatch.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Update failed - invalid village or activity"));
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Exception occurred while updating status: " + e.getMessage());
            e.printStackTrace(); // Full error details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal error occurred", "error", e.getMessage()));
        }
    }

    @GetMapping("/confirmedBookings")
    public String viewConfirmedBookings(@RequestParam(required = false) Long  bookingId,
                                        @RequestParam(required = false) String bookingDate,
                                         @RequestParam(required = false) String activityName,
                                         @RequestParam(required = false) Long villageId,
                                         Model model , HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        List<ActivityBooking> filteredBookings = activityBookingService.getFilteredConfirmedBookings(bookingId,bookingDate, activityName, villageId);

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("confirmedBookings", filteredBookings);
        model.addAttribute("bookingDate", bookingDate);
        model.addAttribute("activityName", activityName);
        model.addAttribute("villageId", villageId);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("village", village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-confirmed-bookings";
    }


    @GetMapping("/pendingBookings")
    public String viewPendingBookings(@RequestParam(required = false) Long bookingId,
                                      @RequestParam(required = false) String bookingDate,
                                      @RequestParam(required = false) String activityName,
                                      @RequestParam(required = false) Long villageId,
                                      Model model , HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        List<ActivityBooking> filteredBookings = activityBookingService.getFilteredPendingBookings(bookingId,bookingDate, activityName, villageId);

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("pendingBookings", filteredBookings);
        model.addAttribute("bookingDate", bookingDate);
        model.addAttribute("activityName", activityName);
        model.addAttribute("villageId", villageId);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("village", village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-pending-bookings";
    }

    @PostMapping("/cancel-booking")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(@RequestBody Map<String, String> payload) {
        Long bookingId = Long.parseLong(payload.get("bookingId"));
        String reason = payload.get("reason");

        boolean success = activityBookingService.cancelBooking(bookingId, reason);

        if (success) {
            ActivityBooking booking = activityBookingService.getBookingsById(bookingId);

            StringBuilder msg = new StringBuilder("❌ Your booking for activity '");
            msg.append(booking.getActivity().getActivityName())
                    .append("' on ").append(booking.getBookingDate());

            if (booking.getTimeSlot() != null) {
                msg.append(" at ").append(booking.getTimeSlot());
            }

            msg.append(" in village '").append(booking.getVillage().getVillageName())
                    .append("' has been cancelled. Reason: ").append(reason)
                    .append(". If you’ve already paid, your refund will be processed shortly. For help, contact ")
                    .append(booking.getVillage().getVillageEmail())
                    .append(" with your booking ID: ").append(booking.getBookingId()).append(".");

            notificationService.sendInboxMessageToUserBookingCancelled(
                    booking.getUser().getUserId(),
                    msg.toString(),
                    booking.getActivity(),
                    booking,
                    booking.getVillage()
            );

            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Cancellation failed"));
        }
    }


    @GetMapping("/cancelledOrExpiredBookings")
    public String viewCancelledOrExpiredBookings(@RequestParam(required = false) Long bookingId,
                                                 @RequestParam(required = false) String bookingDate,
                                                 @RequestParam(required = false) String activityName,
                                                 @RequestParam(required = false) Long villageId,
                                                 @RequestParam(required = false) String status,
                                                 Model model , HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        List<ActivityBooking> filteredBookings = activityBookingService.getFilteredCancelledOrExpiredBookings(bookingId,bookingDate, activityName, villageId ,status);

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("cancelledOrExpiredBookings", filteredBookings);
        model.addAttribute("bookingDate", bookingDate);
        model.addAttribute("activityName", activityName);
        model.addAttribute("status", status);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("villageId", villageId);
        model.addAttribute("village", village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-cancelled-exp-bookings";
    }


    @GetMapping("/endedBookings")
    public String viewEndedBookings(@RequestParam(required = false) Long bookingId,
                                    @RequestParam(required = false) String bookingDate,
                                    @RequestParam(required = false) String activityName,
                                    @RequestParam(required = false) Long villageId,
                                    Model model , HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        List<ActivityBooking> filteredBookings = activityBookingService.getFilteredEndedBookings(bookingId, bookingDate, activityName, villageId);

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("endedBookings", filteredBookings);
        model.addAttribute("bookingDate", bookingDate);
        model.addAttribute("activityName", activityName);
        model.addAttribute("villageId", villageId);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("village", village);
        return "village-past-bookings";
    }

    @GetMapping("/bookingFeedback/{bookingId}")
    public ResponseEntity<BookingsFeedBackDto> getBookingFeedback(@PathVariable Long bookingId) {
        ActivityBooking booking = activityBookingService.getBookingsById(bookingId);
        if (booking != null) {
            BookingsFeedBack feedback = booking.getBookingFeedback();
            if (feedback == null) {
                return ResponseEntity.noContent().build();
            }

            BookingsFeedBackDto dto = new BookingsFeedBackDto();
            dto.setRating(feedback.getRating());
            dto.setFeedback(feedback.getFeedback());
            dto.setSubmittedAt(feedback.getSubmittedAt());
            dto.setSubmittedBy(feedback.getSubmittedBy());

            return ResponseEntity.ok(dto);
        }
        else  {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/bookingDetails/{bookingId}")
    @ResponseBody
    public ResponseEntity<?> getBookingDetails(@PathVariable Long bookingId) {
        try {
            ActivityBookingDto dto = activityBookingService.getBookingDetails(bookingId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }
    }

    @GetMapping("/stay/register")
    public String showVillageStayForm(Model model ,HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        VillageStayDto dto = new VillageStayDto();

        dto.setVillageId(village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("village", village);
        model.addAttribute("villageStayDto", dto);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-add-stay";
    }

    @PostMapping("/addStay")
    public String createStay(@Valid @ModelAttribute("villageStayDto") VillageStayDto villageStayDto, BindingResult result, Model model,RedirectAttributes redirectAttributes, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.toString()));
            model.addAttribute("village", village);
            return "village-add-stay";
        }
        boolean isNameTaken = villageStayService.isStayNameTaken(villageStayDto.getVillageStayName(),villageStayDto.getVillageId());
        if (isNameTaken) {
            model.addAttribute("village", village);
            model.addAttribute("errorMsg", "Stay Already Exist");
            return "village-add-stay";
        }
        VillageStay savedStay = villageStayService.saveStay(villageStayDto);
        if (savedStay == null) {
            model.addAttribute("village", village);
            model.addAttribute("errorMsg", "Activity Registration Failed");
            return "village-add-stay";
        }
        redirectAttributes.addFlashAttribute("StayAdded", true);
        return "redirect:/village/stays";
    }

    @PostMapping("/stays/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStayStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        boolean isActive = payload.get("isActive");
        villageStayService.updateStatus(id, isActive);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stay/{stayId}")
    public ResponseEntity<VillageStayDto> getStayDetails(@PathVariable Long stayId) {
        VillageStayDto dto = villageStayService.getStayDetailsById(stayId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/stays")
    public String showStays(@RequestParam(required = false) Long filterStayId,
                            @RequestParam(required = false) String filterStayName,
                            @RequestParam(required = false) Boolean filterStatus,
                            Model model , HttpSession session) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        List<VillageStay> stays = villageStayService.filterStaysByVillageId(filterStayId, filterStayName, filterStatus,village.getVillageId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("listStays", stays);
        model.addAttribute("village", village);
        model.addAttribute("filterStayId", filterStayId);
        model.addAttribute("filterStayName", filterStayName);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-stays";
    }

    @PatchMapping("/updateStayImage/{stayId}/{oldImageName}")
    public ResponseEntity<String> updateStayImage(
            @PathVariable Long stayId,
            @PathVariable String oldImageName,
            @RequestParam("file") MultipartFile newImage) {

        try {
            villageStayService.updateStayImage(stayId, oldImageName, newImage);
            return ResponseEntity.ok("Image updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update image.");
        }
    }

    @DeleteMapping("/deleteStayImage/{stayId}/{imageName}")
    public ResponseEntity<String> deleteStayImage(
            @PathVariable Long stayId,
            @PathVariable String imageName) {

        try {
            villageStayService.deleteStayImage(stayId, imageName);
            return ResponseEntity.ok("Image deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete image.");
        }
    }

    @PostMapping("/addMoreStayImages/{stayId}")
    public ResponseEntity<String> addMoreStayImages(
            @PathVariable Long stayId,
            @RequestParam("files") List<MultipartFile> files) {

        try {
            villageStayService.addStayImages(stayId, files);
            return ResponseEntity.ok("Images added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add images.");
        }
    }

    @GetMapping("/editStay/{stayId}")
    public String editStay(@PathVariable("stayId") Long stayId, Model model, HttpSession session ,RedirectAttributes redirectAttributes) {


        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        VillageStay villageStay = villageStayService.getVillageStayById(stayId);
        if (villageStay == null) {
            model.addAttribute("errorMsg", "Room not found!");
            return "error-page";
        }

        VillageStayDto stay = new VillageStayDto();
        stay.setStayId(stayId);
        stay.setVillageId(villageStay.getVillage().getVillageId());
        stay.setVillageStayName(villageStay.getVillageStayName());
        stay.setStayType(villageStay.getStayType());
        stay.setDescription(villageStay.getDescription());
        stay.setStayPlace(villageStay.getStayPlace());
        stay.setContactEmail(villageStay.getContactEmail());
        stay.setContactNo(villageStay.getContactNo());
        stay.setContactPersonName(villageStay.getContactPersonName());
        stay.setAltContactNo(villageStay.getAltContactNo());
        stay.setPriceStartsFrom(villageStay.getPriceStartsFrom());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();


        model.addAttribute("stayDto",stay);
        model.addAttribute("village", village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-edit-stay";
    }

    @PostMapping("/updateStay")
    public String updateStay(@Valid VillageStayDto stayDto,BindingResult result,Model model
            ,HttpSession session , RedirectAttributes redirectAttributes) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        if (result.hasErrors()) {
            System.out.println("Validation errors:");
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage())); //
            model.addAttribute("village",village);
            return "village-edit-stay";
        }
        String status = villageStayService.updateStay(stayDto);
        if (!status.equals("success")){
            model.addAttribute("village",village);
            model.addAttribute("errorMsg",status);
            return "village-edit-stay";
        }

        redirectAttributes.addFlashAttribute("stayAdded", true);
        redirectAttributes.addFlashAttribute("stayIdToOpen", stayDto.getStayId());
        return "redirect:/village/stays";
    }

    @GetMapping("/room/register")
    public String showRoomForm(@RequestParam Long stayId, Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        VillageStay stay = villageStayService.getVillageStayById(stayId);

        StayRoomDetailDto dto = new StayRoomDetailDto();

        dto.setStayId(stay.getStayId());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();


        model.addAttribute("village", village);
        model.addAttribute("roomDto", dto);
        model.addAttribute("stayId", stayId); // pass to Thymeleaf
        return "village-add-room";
    }

    @PostMapping("/saveRoom")
    public String createStay(@Valid @ModelAttribute("roomDto") StayRoomDetailDto stayRoomDetailDto, BindingResult result, Model model,RedirectAttributes redirectAttributes, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.toString()));
            model.addAttribute("village", village);
            return "village-add-room";
        }
        boolean isNameTaken = roomService.isRoomNameTaken(stayRoomDetailDto.getRoomName(),stayRoomDetailDto.getStayId());
        if (isNameTaken) {
            model.addAttribute("village", village);
            model.addAttribute("errorMsg", "Room Already Exist");
            return "village-add-room";
        }
        StayRoomDetails savedRoom = roomService.saveRoom(stayRoomDetailDto);
        if (savedRoom == null) {
            model.addAttribute("village", village);
            model.addAttribute("errorMsg", "Activity Registration Failed");
            return "village-add-room";
        }
        redirectAttributes.addFlashAttribute("roomAdded", true);
        redirectAttributes.addFlashAttribute("stayIdToOpen", stayRoomDetailDto.getStayId());
        return "redirect:/village/stays";
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<StayRoomDetailDto> getRoomDetails(@PathVariable Long stayId) {
        StayRoomDetailDto dto = roomService.getRoomDetailsById(stayId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/editRoom/{roomId}")
    public String editRoom(@PathVariable("roomId") Long roomId, Model model, HttpSession session ,RedirectAttributes redirectAttributes) {


        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        StayRoomDetails roomDetails = roomService.getRoomById(roomId);
        if (roomDetails == null) {
            model.addAttribute("errorMsg", "Room not found!");
            return "error-page";
        }

        StayRoomDetailDto room = new StayRoomDetailDto();
        room.setId(roomDetails.getRoomId());
        room.setStayId(roomDetails.getVillageStay().getStayId());
        room.setRoomName(roomDetails.getRoomName());
        room.setNumberOfRooms(roomDetails.getNumberOfRooms());
        room.setAC(roomDetails.isAC());
        room.setAvailableRooms(roomDetails.getAvailableRooms());
        room.setPricePerRoom(roomDetails.getPricePerRoom());
        room.setAmenities(roomDetails.getAmenities());
        room.setNotes(roomDetails.getNotes());
        room.setRoomType(roomDetails.getRoomType());

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();


        model.addAttribute("roomDto",room);
        model.addAttribute("village", village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-edit-room";
    }

    @PostMapping("/updateRoom")
    public String updateRoom(@Valid StayRoomDetailDto roomDto,BindingResult result,Model model
            ,HttpSession session , RedirectAttributes redirectAttributes) {

        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }

        if (result.hasErrors()) {
            System.out.println("Validation errors:");
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage())); //
            model.addAttribute("village",village);
            return "village-edit-room";
        }
        String status = roomService.updateRoom(roomDto);
        if (!status.equals("success")){
            model.addAttribute("village",village);
            model.addAttribute("errorMsg",status);
            return "village-edit-room";
        }

        redirectAttributes.addFlashAttribute("roomAdded", true);
        redirectAttributes.addFlashAttribute("stayIdToOpen", roomDto.getStayId());
        return "redirect:/village/stays";
    }

    @PostMapping("/addMoreRoomImages/{roomId}")
    public ResponseEntity<?> addImages(@PathVariable Long roomId,
                                       @RequestParam("files") List<MultipartFile> files) {
        return roomService.handleAddMoreImages(roomId, files);
    }

    @PatchMapping("/updateRoomImage/{roomId}/{oldImageName}")
    public ResponseEntity<?> updateImage(@PathVariable Long roomId,
                                         @PathVariable String oldImageName,
                                         @RequestParam("file") MultipartFile newFile) {
        return roomService.handleUpdateImage(roomId, oldImageName, newFile);
    }

    @DeleteMapping("/deleteRoomImage/{roomId}/{imageName}")
    public ResponseEntity<?> deleteImage(@PathVariable Long roomId,
                                         @PathVariable String imageName) {
        return roomService.handleDeleteImage(roomId, imageName);
    }




    @GetMapping("/feedbacks")
    public String feedBacks(Model model, HttpSession session) {
        Village village = (Village) session.getAttribute("loggedInVillage");
        if (village == null) {
            return "redirect:/loginPage";
        }
        List<UserFeedBack> feedBacks = userFeedBackRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        List<VillageInboxMessage> unreadMessages = notificationService.getUnreadVillageMessagesCount(village);
        int unreadCount = notificationService.getUnreadVillageMessagesCount(village).size();

        model.addAttribute("feedbacks", feedBacks);
        model.addAttribute("village", village);
        model.addAttribute("villageMessages", unreadMessages);
        model.addAttribute("unreadCount", unreadCount);
        return "village-feedbacks";
    }

}

