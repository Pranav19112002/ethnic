package com.ev.Controller;

import com.ev.Model.*;
import com.ev.Repository.AdminInboxRepository;
import com.ev.Repository.UserFeedBackRepository;
import com.ev.Services.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VillageService villageService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private TempVillageService tempVillageService;

    @Autowired
    private AdminDashboardServices adminDashboardServices;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserFeedBackRepository userFeedBackRepository;

    @GetMapping("/home")
    public String viewAdminHomePage(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }

        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();
        String formattedUserCount = adminDashboardServices.getFormattedUserCount();
        String formattedVillageCount = adminDashboardServices.getFormattedVillageCount();
        String formattedActivityCount = adminDashboardServices.getFormattedActivityCount();
        String formattedBookingCount = adminDashboardServices.getFormattedBookingCount();
        String formattedStarCount = adminDashboardServices.getFormattedStarCount();

        model.addAttribute("admin", loggedInAdmin);
        model.addAttribute("messages", inboxMessages);
        model.addAttribute("userCount", formattedUserCount);
        model.addAttribute("villageCount", formattedVillageCount);
        model.addAttribute("activityCount", formattedActivityCount);
        model.addAttribute("bookingCount", formattedBookingCount);
        model.addAttribute("starCount", formattedStarCount);
        model.addAttribute("availableCount", adminDashboardServices.getFormattedAvailableCount());
        model.addAttribute("unavailableCount", adminDashboardServices.getFormattedUnavailableCount());
        model.addAttribute("endedCount", adminDashboardServices.getFormattedEndedCount());


        return "admin-homepage";
    }

    @GetMapping("/requests")
    public String viewVillageRequests(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        List<Village> villages = villageService.getVillageRequests();

        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();
        model.addAttribute("messages", inboxMessages);
        model.addAttribute("villages", villages);
        model.addAttribute("admin", loggedInAdmin);
        return "admin-village-req";
    }

    @PostMapping("/updateVillageStatus")
    public ResponseEntity<Map<String, Object>> updateVillageStatus(@RequestBody Map<String, String> requestData) {
        try {
            Long villageId = Long.parseLong(requestData.get("villageId"));
            String status = requestData.get("status");

            Village isUpdated = villageService.updateVillageStatus(villageId, status);

            if (isUpdated != null) {
                String email = isUpdated.getVillageEmail();
                String villageName = isUpdated.getVillageName();
                LocalDateTime timestamp = isUpdated.getApprovedOrRejectedAt();
                String updatedStatus = isUpdated.getVillageStatus();

                mailService.sendVillageStatusUpdateEmail(email, villageName, updatedStatus, timestamp);

                if ("approved".equalsIgnoreCase(updatedStatus)) {
                    String welcomeMessage = "Welcome " + villageName + " to the Ethnic Village World!";
                    notificationService.sendVillageNotification(isUpdated, VillageNotificationType.ADMIN_APPROVAL, welcomeMessage);
                }

                return ResponseEntity.ok(Map.of("success", true, "message", "Village status updated!"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Update failed!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/viewAllVillages")
    public String viewAllVillages(@RequestParam(value = "selectedCurrentStatus", required = false) String selectedCurrentStatus,
                                  @RequestParam(value = "searchVillageName", required = false, defaultValue = "") String searchVillageName,
                                  @RequestParam(value = "villageStatusFilter", required = false) String villageStatusFilter,
                                  Model model, HttpSession session) {

        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        if (searchVillageName != null && !searchVillageName.isEmpty()) {
            selectedCurrentStatus = null;
        }
        if (selectedCurrentStatus != null && !selectedCurrentStatus.equalsIgnoreCase("All") && !selectedCurrentStatus.isEmpty()) {
            searchVillageName = null;
        }
        List<Village> villages = villageService.getAllFilteredVillagesForAdmin(searchVillageName, selectedCurrentStatus, villageStatusFilter);

        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();

        model.addAttribute("messages", inboxMessages);
        model.addAttribute("villages", villages);
        model.addAttribute("selectedCurrentStatus", selectedCurrentStatus);
        model.addAttribute("searchVillageName", searchVillageName);
        model.addAttribute("villageStatusFilter", villageStatusFilter);
        model.addAttribute("admin", loggedInAdmin);
        return "admin-view-villages";
    }

    @GetMapping("/viewPendingVillageEditRequests")
    public String viewPendingVillageEditRequests(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }

        String status = "Pending";
        List<TempVillage> tempVillages = tempVillageService.getAllTempVillagesByStatus(status);

        for (TempVillage tv : tempVillages) {
            System.out.println("VillageId: " + (tv != null ? tv.getVillageId() : "NULL"));
        }

        Map<Long, Village> currentVillageMap = villageService.getCurrentVillageMapForVillageUpdate(tempVillages);
        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();

        model.addAttribute("messages", inboxMessages);
        model.addAttribute("tempVillageRequests", tempVillages);
        model.addAttribute("currentVillageMap", currentVillageMap);
        model.addAttribute("admin", loggedInAdmin);

        return "admin-village-edit-req";
    }

    @PostMapping("/approveVillageUpdateRequest/{requestId}")
    @ResponseBody
    public ResponseEntity<String> approveVillageUpdate(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> updatePayload) {

        try {
            Village updatedVillage = villageService.approveVillageUpdateRequest(requestId, updatePayload);
            if (updatedVillage == null) {
                return ResponseEntity.ok("No changes detected.");
            }
            String message = "Your update request has been approved. The new details are now live.";
            notificationService.sendVillageNotification(updatedVillage, VillageNotificationType.ADMIN_UPDATE, message);
            return ResponseEntity.ok("Village update approved.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing approval.");
        }
    }

    @PostMapping("/rejectVillageUpdateRequest/{id}")
    @ResponseBody
    public ResponseEntity<String> rejectVillageUpdateRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Model model) {

        String message = payload.get("message");

        try {
            TempVillage isUpdated = villageService.rejectVillageUpdateRequest(id, message);
            if (isUpdated != null) {
                Village village = villageService.getVillageById(isUpdated.getVillageId());
                String notificationMessage = "Your update request has been rejected. Please Try again later.";
                notificationService.sendVillageNotification(village, VillageNotificationType.ADMIN_UPDATE, notificationMessage);
                return ResponseEntity.ok("Village update rejected.");
            } else {
                return ResponseEntity.status(400).body("Rejection failed: no matching update request.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing rejection.");
        }
    }

    @GetMapping("/updatedVillages")
    public String viewUpdateVillageRequests(@RequestParam(value = "statusFilter", required = false, defaultValue = "All") String statusFilter,
                                            Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }

        List<TempVillage> villageRequests;
        if ("All".equalsIgnoreCase(statusFilter)) {
            villageRequests = tempVillageService.getAllVillageUpdates();
        } else {
            villageRequests = tempVillageService.getVillageUpdatesByStatus(statusFilter);
        }
        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();

        model.addAttribute("messages", inboxMessages);
        model.addAttribute("villageRequests", villageRequests);
        model.addAttribute("admin", loggedInAdmin);
        model.addAttribute("statusFilter", statusFilter);
        return "admin-updated-villages";
    }

    @GetMapping("/viewActivityRequests")
    public String showActivities(@RequestParam(value = "selectedType", required = false) String selectedType,
                                 @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
                                 Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        List<String> activityTypes = activityService.getAllActivityTypes();
        List<Activity> activities;
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            activities = activityService.searchActivitiesRequestsByName(searchKeyword);
            selectedType = "";
        } else if (selectedType != null && !selectedType.equalsIgnoreCase("All") && !selectedType.isEmpty()) {
            activities = activityService.searchActivitiesRequestsByType(selectedType);
        } else {
            activities = activityService.getAllActivityRequests();
            selectedType = "All";
        }
        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();

        model.addAttribute("messages", inboxMessages);
        model.addAttribute("admin", loggedInAdmin);
        model.addAttribute("activityTypes", activityTypes);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("activities", activities);
        return "admin-activity-req";
    }

    @PostMapping("/updateActivityStatus")
    public ResponseEntity<Map<String, Object>> updateActivityStatus(@RequestBody Map<String, String> requestData) {
        try {
            Long activityId = Long.parseLong(requestData.get("activityId"));
            Long villageId = Long.parseLong(requestData.get("villageId"));
            String status = requestData.get("status");

            boolean isUpdated = activityService.updateRequestStatus(activityId, status, villageId);

            if (isUpdated) {
                Activity activity = activityService.getActivityById(activityId);
                Village village = villageService.getVillageById(villageId);

                String message = switch (status.toUpperCase()) {
                    case "APPROVED" -> "Your activity \"" + activity.getActivityName() + "\" has been approved.";
                    case "REJECTED" -> "Your activity \"" + activity.getActivityName() + "\" was rejected.";
                    case "STARTED" -> "Activity \"" + activity.getActivityName() + "\" has started.";
                    case "ENDED" -> "Activity \"" + activity.getActivityName() + "\" has ended.";
                    default -> "Status of activity \"" + activity.getActivityName() + "\" was updated.";
                };

                notificationService.sendVillageNotification(village,VillageNotificationType.ACTIVITY_REQUEST_STATUS_CHANGE,message,activity);
                return ResponseEntity.ok(Map.of("success", true, "message", "Request status updated!"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Update failed!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/viewAllActivities")
    public String showAllActivities(
            @RequestParam(value = "selectedType", required = false) String selectedType,
            @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
            @RequestParam(value = "villageId", required = false) Long villageId,
            @RequestParam(value = "statusFilter", required = false) String statusFilter,
            Model model, HttpSession session) {

        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        System.out.println("Received Parameters:");
        System.out.println("selectedType = " + selectedType);
        System.out.println("searchKeyword = " + searchKeyword);
        System.out.println("villageId = " + villageId);
        System.out.println("statusFilter = " + statusFilter);
        List<String> activityTypes = activityService.getAllActivityTypes();

        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            selectedType = null;
            villageId = null;
        }
        if (villageId != null) {
            selectedType = null;
            searchKeyword = null;
        }
        if (selectedType != null && !selectedType.equalsIgnoreCase("All") && !selectedType.isEmpty()) {
            searchKeyword = null;
            villageId = null;
        }

        List<Activity> activities = activityService.getAllFilteredActivitiesForAdmin(searchKeyword, villageId, selectedType, statusFilter);

        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();

        System.out.println("Filtered Activities: " + activities.size());
        model.addAttribute("admin", loggedInAdmin);
        model.addAttribute("messages", inboxMessages);
        model.addAttribute("activityTypes", activityTypes);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("villageId", villageId);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("activities", activities);

        return "admin-all-activities";
    }

    @GetMapping("/allUsers")
    public String viewAllUsers(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();
        List<User> users = userService.findAllUsers();

        model.addAttribute("users", users);
        model.addAttribute("admin", loggedInAdmin);
        model.addAttribute("messages", inboxMessages);
        return "admin-users";
    }

    @PostMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestParam Long userId) {
        User user = userService.getUserById(userId);
        String userEmail = user.getUserEmail();
        String success = userService.deleteUserById(userId);
        if ("success".equals(success)) {
            mailService.sendUserRemovalEmail(userEmail, LocalDateTime.now());
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete user: " + success);
        }
    }

    @GetMapping("/profile")
    public String viewAdminProfile(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        model.addAttribute("admin", loggedInAdmin);
        return "admin-profile";
    }

    @GetMapping("/viewUpdateAdmin")
    public String viewAdminEdit(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        AdminDto adminDto = new AdminDto();
        adminDto.setAdminName(loggedInAdmin.getAdminName());
        adminDto.setAdminContactNo(loggedInAdmin.getAdminContactNo());
        adminDto.setAdminEmail(loggedInAdmin.getAdminEmail());
        adminDto.setAdminId(loggedInAdmin.getAdminId());
        adminDto.setAdminProfileImage(loggedInAdmin.getAdminProfileImage());
        model.addAttribute("adminDto", adminDto);
        model.addAttribute("admin", loggedInAdmin);
        return "admin-profile-edit";
    }
    @PostMapping("/updateAdmin")
    public String updateAdminDetails(@Valid @ModelAttribute("adminDto") AdminDto adminDto,
                                     BindingResult result,
                                     Model model,
                                     HttpSession session) {
        System.out.println("✅ Controller called: /admin/updateAdmin");

        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }

        if (result.hasErrors()) {
            model.addAttribute("resultErrorMsg", result);
            result.getAllErrors().forEach(e -> System.out.println("❌ Validation Error: " + e.getDefaultMessage()));
            return "admin-profile-edit";
        }

        boolean imageWasUpdated = adminDto.getAdminProfileImageFile() != null &&
                !adminDto.getAdminProfileImageFile().isEmpty();

        String status = adminService.updateAdmin(adminDto, loggedInAdmin.getAdminId());
        System.out.println("Update Status: " + status);

        if (status.equals("success")) {
            Admin updatedAdmin = adminService.findAdminById(loggedInAdmin.getAdminId());
            if (updatedAdmin != null) {
                session.setAttribute("loggedInAdmin", updatedAdmin);

                AdminDto updatedDto = new AdminDto();
                updatedDto.setAdminName(updatedAdmin.getAdminName());
                updatedDto.setAdminEmail(updatedAdmin.getAdminEmail());
                updatedDto.setAdminContactNo(updatedAdmin.getAdminContactNo());
                updatedDto.setAdminProfileImage(updatedAdmin.getAdminProfileImage());

                model.addAttribute("adminDto", updatedDto);
            }

            model.addAttribute("successMessage", "Profile updated successfully!");
            if (imageWasUpdated) {
                model.addAttribute("imageUpdated", true); // 🔔 Trigger SweetAlert in view
            }

            return "admin-profile-edit";
        } else {
            model.addAttribute("admin", loggedInAdmin);
            model.addAttribute("adminDto", adminDto);
            model.addAttribute("errorMessage", status);
            return "admin-profile-edit";
        }
    }


    @GetMapping("/viewResetPassword")
    public String viewPasswordResetPage(AdminDto adminDto, Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        model.addAttribute("adminDto", adminDto);
        model.addAttribute("admin", loggedInAdmin);
        return "admin-profile-resetpass";
    }

    @PostMapping("/reset-password")
    public String updateAdminPassword(AdminDto adminDto, Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        if (!adminDto.getNewPassword().equals(adminDto.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "admin-profile-resetpass";
        }
        Admin updatedAdmin = adminService.updateAdminPassword(loggedInAdmin.getAdminId(), adminDto.getNewPassword());
        if (updatedAdmin != null) {
            session.setAttribute("loggedInAdmin", updatedAdmin);
            model.addAttribute("passwordResetSuccess", true); // ✅ Flag for SweetAlert
            return "admin-profile-resetpass"; // Stay on the same page
        }
        model.addAttribute("adminDto", adminDto);
        model.addAttribute("admin", loggedInAdmin);
        return "admin-profile-resetpass";
    }

    @GetMapping("/feedbacks")
    public String feedBacks(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        List<UserFeedBack> feedBacks = userFeedBackRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<AdminInboxMessage> inboxMessages = notificationService.getAllNotificationsByTime();

        model.addAttribute("messages", inboxMessages);
        model.addAttribute("feedBacks", feedBacks);
        model.addAttribute("admin", loggedInAdmin);
        return "admin-user-feedbacks";
    }


    @GetMapping("/logout")
    public String logoutAdmin(Model model, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        model.addAttribute("admin", loggedInAdmin);
        return "admin-logout";
    }

    @PostMapping("/logoutNow")
    public String performLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/loginPage";
    }


    @GetMapping("/inbox")
    public String showInbox(Model model , HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return "redirect:/loginPage";
        }
        List<AdminInboxMessage> messages = notificationService.getAllMessages();
        model.addAttribute("messages", messages);
        return "admin-notifications";
    }

    @PostMapping("/inbox/markRead/{id}")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long id) {
        notificationService.markMessageAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inbox/markAllRead")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAdminMessagesAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/inbox/clearAll")
    public ResponseEntity<Void> clearAll() {
        notificationService.deleteAllAdminMessages();
        return ResponseEntity.ok().build();
    }

}
