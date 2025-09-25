package com.ev.Controller;

import com.ev.Model.*;
import com.ev.Repository.UserFeedBackRepository;
import com.ev.Services.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private  VillageService villageService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserFeedBackRepository userFeedBackRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VillageStayService villageStayService;

    @Autowired
    private MailService mailService;



    @GetMapping("/")
    public String viewUserHome(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);

        boolean feedbackGiven = loggedInUser != null && userFeedBackRepository.existsByUser(loggedInUser);
        model.addAttribute("feedbackGiven", feedbackGiven);

        List<Activity> approvedActivities = activityService.getActivityByRequestStatusAndActivityStatus();
        model.addAttribute("activities", approvedActivities);

        List<UserFeedBack> feedbacks = userFeedBackRepository.findTop15ByOrderBySubmittedAtDesc();
        model.addAttribute("feedbacks", feedbacks);

        if (loggedInUser != null) {
            int unreadCount = notificationService.getUnreadMessagesForUser(loggedInUser.getUserId()).size();
            model.addAttribute("unreadCount", unreadCount);
        }
        return "index";
    }

    @GetMapping("/villages")
    public String viewVillages(
            @RequestParam(required = false) String activityName,
            @RequestParam(required = false) String villageName,
            @RequestParam(required = false) String region,
            Model model,
            HttpSession session
    ) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);

        if (loggedInUser != null) {
            int unreadCount = notificationService.getUnreadMessagesForUser(loggedInUser.getUserId()).size();
            model.addAttribute("unreadCount", unreadCount);
        }

        List<Activity> activities = activityService.filterActivities(activityName, villageName, region);
        model.addAttribute("activities", activities);

        model.addAttribute("activityName", activityName);
        model.addAttribute("villageName", villageName);
        model.addAttribute("region", region);
        return "villages";
    }





    @GetMapping("/contact")
    public String viewContact(Model model , HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);

        if (loggedInUser != null) {
            int unreadCount = notificationService.getUnreadMessagesForUser(loggedInUser.getUserId()).size();
            model.addAttribute("unreadCount", unreadCount);
        }
        return "contact"; // your homepage view
    }

    @PostMapping("/contact/send")
    public String sendContactMessage(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes
    ) {
        mailService.sendContactMessage(name, email, subject, message);
        redirectAttributes.addFlashAttribute("success", "Your message has been sent successfully!");
        return "redirect:/contact";
    }



    @GetMapping("/about")
    public String viewAbout(Model model , HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);

        List<UserFeedBack> feedbacks = userFeedBackRepository.findTop15ByOrderBySubmittedAtDesc();
        model.addAttribute("feedbacks", feedbacks);

        if (loggedInUser != null) {
            int unreadCount = notificationService.getUnreadMessagesForUser(loggedInUser.getUserId()).size();
            model.addAttribute("unreadCount", unreadCount);
        }

        return "about"; // your homepage view
    }

    @GetMapping("/stays")
    public String viewStays(
            @RequestParam(required = false) String villageName,
            @RequestParam(required = false) String region,
            Model model,
            HttpSession session
    ) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);

        if (loggedInUser != null) {
            int unreadCount = notificationService.getUnreadMessagesForUser(loggedInUser.getUserId()).size();
            model.addAttribute("unreadCount", unreadCount);
        }

        List<VillageStay> stays = villageStayService.filterStays(villageName, region);
        model.addAttribute("stays", stays);

        return "stays";
    }



    @GetMapping("/loginPage")
    public String viewLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginDto());
        return "login-page";
    }

    @PostMapping("/login")
    public String loginCheck(@Valid @ModelAttribute("loginForm")LoginDto loginDto, BindingResult result, Model model , HttpSession session)
    {
        if (result.hasErrors()){
            return "login-page";
        }

        String emailOrId = loginDto.getEmailOrId();
        String password = loginDto.getPassword();

        User user = userService.checkUserExists(emailOrId , password);
        if (user != null) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        }

        Village village = villageService.checkVillageExists(emailOrId , password);
        if (village != null){
            session.setAttribute("loggedInVillage" , village);
            return "redirect:/village/home";
        }

        Admin admin = adminService.checkAdminExits(emailOrId , password);
        if (admin != null){
            session.setAttribute("loggedInAdmin",admin);
            return "redirect:/admin/home";
        }
        model.addAttribute("errorMessage","Invalid Credentials");
        return "login-page";
    }

    @GetMapping("/home/locations/search")
    @ResponseBody
    public List<String> searchLocations(@RequestParam String query) {
        return villageService.findVillageLocationsContaining(query);
    }

    @GetMapping("/home/villages/search")
    @ResponseBody
    public List<Village> searchVillages(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String village) {
        return villageService.findAvailableVillagesByLocationAndVillageName(location, village);
    }

    @GetMapping("/home/activities/byVillage")
    @ResponseBody
    public List<ActivityDto> getActivitiesByVillage(@RequestParam String villageName) {
        return activityService.getApprovedActivitiesByVillageName(villageName);
    }

    @GetMapping("/home/activities/search")
    public List<ActivityDto> searchActivitiesByVillageAndPrice(@RequestParam String village,
                                                               @RequestParam Double maxPrice) {
        return activityService.searchApprovedActivitiesByVillageNameAndMaxPrice(village, maxPrice);
    }
}
