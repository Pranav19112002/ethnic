package com.ev.Services;

import com.ev.Model.Activity;
import com.ev.Model.ActivityBooking;
import com.ev.Model.ActivityDto;
import com.ev.Model.Village;
import com.ev.Repository.ActivityBookingRepository;
import com.ev.Repository.ActivityRepository;
import com.ev.Repository.VillageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityServices implements ActivityService{

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private VillageRepository villageRepository;

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MailService mailService;

    @Override
    public boolean isActivityNameTaken(String activityName , Long villageId) {
        return activityRepository.existsByActivityNameAndVillage_VillageId(activityName,villageId);
    }

    @Override
    public Activity getActivityById(Long activityId) {
        Optional<Activity> optionalActivity = activityRepository.findById(activityId);
        return optionalActivity.orElse(null);
    }


    @Override
    public Activity saveActivity(ActivityDto activityDto) {
        LocalDateTime regDate = LocalDateTime.now();
        if (activityDto.getVillageId() == null){
            throw new IllegalArgumentException("Village ID cannot be null");
        }

        List<String> storageFileNames = saveFile(activityDto.getActivityPhotos());

        Activity activity = new Activity();
        activity.setActivityName(activityDto.getActivityName());
        activity.setActivityPlace(activityDto.getActivityPlace());
        activity.setActivityType(activityDto.getActivityType());
        activity.setDescription(activityDto.getDescription());
        activity.setDuration(activityDto.getDuration());
        activity.setPrice(activityDto.getPrice());
        activity.setRegisteredDate(regDate);
        activity.setActivityPhotosNames(storageFileNames);
        Village village = villageRepository.findById(activityDto.getVillageId())
                .orElseThrow(() -> new RuntimeException("Village not found"));
        activity.setVillage(village);

        if ("seasonal".equalsIgnoreCase(activityDto.getActivityType()) ||
                "permanent".equalsIgnoreCase(activityDto.getActivityType()) ||
                    "weatherDependent".equalsIgnoreCase(activityDto.getActivityType())){
            List<String> selectedDays = activityDto.getAvailableDays();
            if (selectedDays == null || selectedDays.isEmpty()){
                selectedDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
                activity.setAvailableDays(selectedDays);
            }
            activity.setAvailableDays(selectedDays);
            activity.setTimeSlots(activityDto.getTimeSlots());
            activity.setNoOfPeopleAllowedForSloted(activityDto.getNoOfPeopleAllowedForSloted());
        }

        if("weatherDependent".equalsIgnoreCase(activityDto.getActivityType())){
            if(activityDto.getAlternativeActivityId() != null && activityDto.getAlternativeActivityId() != 0){
               Optional<Activity> optionalActivity = activityRepository.findById(activityDto.getAlternativeActivityId());
               if (optionalActivity.isPresent()){
                   Activity altAct = optionalActivity.get();
                   activity.setAlternativeActivity(altAct);
               }
            }
            else {
                activity.setAlternativeActivity(null);
            }
        }

        if ("seasonal".equalsIgnoreCase(activityDto.getActivityType())){
            activity.setStartDate(activityDto.getStartDate());
            activity.setEndDate(activityDto.getEndDate());
        }
        if ("realTimeEvent".equalsIgnoreCase(activityDto.getActivityType())){
            activity.setEventDateTime(activityDto.getEventDateTime());
            activity.setNoOfPeopleAllowedForDateOnly(activityDto.getNoOfPeopleAllowedForDateOnly());
        }

        activityRepository.save(activity);
        return activity;
    }

    @Override
    public List<String> saveFile(List<MultipartFile> files) {

        List<String> filePaths = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return filePaths;
        }
        try {
            String uploadDir = "src/main/resources/static/uploads/activity/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            for (MultipartFile file : files){
                if (!file.isEmpty()){
                    String originalFilename = file.getOriginalFilename();
                    String sanitizedFilename = sanitizeFilename(originalFilename);
                    String fileName =System.currentTimeMillis() +"_"+sanitizedFilename;
                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);
                    filePaths.add(fileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving files: " + e.getMessage());
        }
        return filePaths;
    }

    @Override
    public String saveSingleFile(MultipartFile file, String uploadDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String fileName = System.currentTimeMillis() + "_" + sanitizedFilename;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("File upload error: " + ex.getMessage());
            return null;
        }

        return fileName;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }


    @Override
    public List<Activity> getAllActivitiesByVillageId(Long villageId) {
        return activityRepository.findByVillage_VillageIdOrderByActivityIdDesc(villageId);
    }

    @Override
    public List<Activity> getAllPendingActivitiesByVillageId(Long villageId) {
        String requestStatus = "Request-send";
        return activityRepository.findByVillage_VillageIdAndRequestStatusIgnoreCaseOrderByActivityIdDesc(villageId,requestStatus);
    }

    @Override
    public List<Activity> getAllActivities() {
        return activityRepository.findAllByOrderByActivityIdDesc();
    }
    @Override
    public List<String> getAllActivityTypes() {
        return activityRepository.findAllVillageTypes();
    }

    @Override
    public List<String> getAllRequestStatuses() {
        return activityRepository.findAllRequestStatuses();
    }



    @Override
    public List<Activity> getAllActivityRequests() {
        return activityRepository.findByRequestStatusOrderByActivityIdDesc("Request-send");
    }

    @Override
    public List<Activity> searchActivitiesByName(String searchKeyword, Long villageId) {
        return activityRepository.findByActivityNameContainingIgnoreCaseAndVillage_VillageId(searchKeyword,villageId);
    }

    @Override
    public List<Activity> searchPendingActivitiesByName(String searchKeyword, Long villageId) {
        String requestStatus = "Request-send";
        return activityRepository.findByActivityNameStartsWithIgnoreCaseAndVillage_VillageIdAndRequestStatusIgnoreCaseOrderByActivityIdDesc(searchKeyword,villageId,requestStatus);
    }

    @Override
    public List<Activity> searchActivitiesRequestsByName(String searchKeyword) {
        String status = "Request-send";
        return activityRepository.findByActivityNameStartsWithIgnoreCaseAndRequestStatus(searchKeyword , status);
    }

    @Override
    public List<Activity> searchAllActivitiesByName(String searchKeyword) {
        return activityRepository.findByActivityNameStartsWithIgnoreCaseOrderByActivityIdDesc(searchKeyword);
    }

    @Override
    public List<Activity> searchAllActivitiesByVillageId(Long villageId) {
        return activityRepository.findByVillage_VillageId(villageId);
    }


    @Override
    public List<Activity> searchActivitiesByType(String villageType, Long villageId) {
        String status ="Approved";
        return activityRepository.findByActivityTypeAndVillage_VillageIdAndRequestStatusIgnoreCaseOrderByActivityIdDesc(villageType , villageId , status);
    }

    @Override
    public List<Activity> searchActivitiesRequestsByType(String villageType) {
        String status = "Request-send";
        return activityRepository.findByActivityTypeAndRequestStatusOrderByActivityIdDesc(villageType , status);
    }

    @Override
    public List<Activity> searchAllActivitiesByType(String villageType) {
        return activityRepository.findByActivityTypeOrderByActivityIdDesc(villageType);
    }

    @Override
    public List<Activity> searchAllActivitiesByNameAndRequestStatus(String activityName, String requestStatus) {
        return activityRepository.findByActivityNameStartsWithIgnoreCaseAndRequestStatusOrderByActivityIdDesc(activityName , requestStatus);
    }

    @Override
    public List<Activity> searchAllActivitiesByTypeAndRequestStatus(String activityType, String requestStatus) {
        return activityRepository.findByActivityTypeAndRequestStatusOrderByActivityIdDesc(activityType,requestStatus);
    }


    @Override
    public List<Activity> searchAllActivitiesByVillageIdAndRequestStatus(Long villageId, String requestStatus) {
        return activityRepository.findByVillage_VillageIdAndRequestStatusOrderByActivityIdDesc(villageId,requestStatus);
    }

    @Override
    public List<Activity> searchAllActivitiesByRequestStatus(String requestStatus) {
        return activityRepository.findByRequestStatusOrderByActivityIdDesc(requestStatus);
    }

    @Override
    public List<Activity> searchActivitiesByReqStatus(String requestStatus, Long villageId) {
        return activityRepository.findByRequestStatusAndVillage_VillageIdOrderByActivityIdDesc(requestStatus , villageId);
    }

    @Override
    public List<Activity> getAllFilteredActivitiesForAdmin(
            String activityName, Long villageId, String activityType, String requestStatus) {

        List<Activity> activities;

        if (activityName != null && !activityName.isEmpty() && requestStatus != null && !requestStatus.isEmpty()) {
            // Search by activity name and status
            return searchAllActivitiesByNameAndRequestStatus(activityName, requestStatus);
        }
        if (activityName != null && !activityName.isEmpty()) {
            // Search only by activity name
            return searchAllActivitiesByName(activityName);
        }
        if (villageId != null && requestStatus != null && !requestStatus.isEmpty()) {
            // Search by village ID and status
            return searchAllActivitiesByVillageIdAndRequestStatus(villageId, requestStatus);
        }
        if (villageId != null) {
            // Search only by village ID
            return searchAllActivitiesByVillageId(villageId);
        }
        if (activityType != null && !activityType.equalsIgnoreCase("All") && !activityType.isEmpty()
                && requestStatus != null && !requestStatus.isEmpty()) {
            // Search by activity type and status
            return searchAllActivitiesByTypeAndRequestStatus(activityType, requestStatus);
        }
        if (activityType != null && !activityType.equalsIgnoreCase("All") && !activityType.isEmpty()) {
            // Search only by activity type
            return searchAllActivitiesByType(activityType);
        }
        if (requestStatus != null && !requestStatus.isEmpty()) {
            // Search only by request status
            return searchAllActivitiesByRequestStatus(requestStatus);
        }

        // Default case: No filters applied, return all activities
        return getAllActivities();
    }


    @Override
    @Transactional
    public boolean updateActivityStatus(Long activityId, String status) {
        Optional<Activity> optionalActivity = activityRepository.findById(activityId);
        if (optionalActivity.isEmpty()) return false;

        Activity activity = optionalActivity.get();
        activity.setActivityStatus(status);
        activityRepository.save(activity);

        if ("Unavailable".equalsIgnoreCase(status)) {
            List<ActivityBooking> bookings = activityBookingRepository
                    .findByActivity_ActivityIdAndStatusInIgnoreCase(activityId, List.of("confirmed", "pending"));

            System.out.println("📦 [BOOKINGS] Found " + bookings.size() + " bookings to cancel for activity: " + activity.getActivityName());

            for (ActivityBooking booking : bookings) {
                booking.setStatus("cancelled");
                booking.setExpiredOrCancelledAt(LocalDateTime.now());

                String reason = "Activity marked as unavailable";
                booking.setExpiredOrCancelReason(reason);
                activityBookingRepository.save(booking);

                System.out.println("❌ [BOOKING] Cancelled booking #" + booking.getBookingId() + " for userId: " + booking.getUser().getUserId());

                // Build message
                StringBuilder msg = new StringBuilder("❌ Your booking for activity '")
                        .append(activity.getActivityName())
                        .append("' on ").append(booking.getBookingDate());

                if (booking.getTimeSlot() != null) {
                    msg.append(" at ").append(booking.getTimeSlot());
                }

                msg.append(" has been cancelled.");

                if ("confirmed".equalsIgnoreCase(booking.getStatus())) {
                    msg.append(" Refund will be provided.");
                }

                // Send inbox message
                notificationService.sendInboxMessageToUserActivityStatusChange(
                        booking.getUser().getUserId(),
                        msg.toString(),
                        activity,
                        booking,
                        activity.getVillage()
                );

                // Send email
                mailService.sendActivityCancellationEmail(
                        booking.getUser().getUserEmail(),
                        activity.getActivityName(),
                        booking.getBookingDate(),
                        reason,
                        booking.getStatus(),
                        null,
                        activity.getVillage().getVillageEmail(),
                        booking.getBookingId()
                );
            }
        }

        return true;
    }




    @Override
    public boolean updateRequestStatus(Long activityId, String status, Long villageId) {
        Optional<Activity> optionalActivity = activityRepository.findById(activityId);
        if (optionalActivity.isPresent())
        {
            Activity activity = optionalActivity.get();
            if (activity.getVillage().getVillageId().equals(villageId)){
                activity.setRequestStatus(status);
                activityRepository.save(activity);
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean deleteActivityById(long activityId) {
        Optional<Activity> optionalActivity = activityRepository.findById(activityId);
        if (optionalActivity.isPresent()) {
            activityRepository.deleteById(activityId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Activity> searchVillageActivityByNameAndActivityStatusAndRequestStatusAndVillageId(String activityName, String activityStatus, String requestStatus, Long villageId) {
        return activityRepository.findByVillage_VillageIdAndActivityNameStartsWithIgnoreCaseAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(villageId,activityName,activityStatus,requestStatus);
    }

    @Override
    public List<Activity> searchVillageActivityByNameAndRequestStatusAndVillageId(String activityName, String requestStatus, Long villageId) {
        return activityRepository.findByVillage_VillageIdAndActivityNameStartsWithIgnoreCaseAndRequestStatusIgnoreCase(villageId,activityName,requestStatus);
    }

    @Override
    public List<Activity> searchVillageActivityByActivityTypeAndActivityStatusAndRequestStatusAndVillageId(String activityType, String activityStatus, String requestStatus, Long villageId) {
        return activityRepository.findByVillage_VillageIdAndActivityTypeIgnoreCaseAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(villageId,activityType,activityStatus,requestStatus);
    }

    @Override
    public List<Activity> searchVillageActivityByActivityTypeAndRequestStatusAndVillageId(String activityType, String requestStatus, Long villageId) {
        return activityRepository.findByVillage_VillageIdAndActivityTypeIgnoreCaseAndRequestStatusIgnoreCase(villageId,activityType,requestStatus);
    }

    @Override
    public List<Activity> searchVillageActivityByActivityStatusAndRequestStatusAndVillageId(String activityStatus, String requestStatus, Long villageId) {
        return activityRepository.findByVillage_VillageIdAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(villageId,activityStatus,requestStatus);
    }

    @Override
    public List<Activity> searchVillageActivityByRequestStatusAndVillageId(String requestStatus, Long villageId) {
        return activityRepository.findByVillage_VillageIdAndRequestStatusIgnoreCase(villageId,requestStatus);
    }

    @Override
    public List<Activity> getAllFilteredActivitiesForVillageAdmin(String activityName, String activityType, String activityStatus, Long villageId) {
        List<Activity> activities;
        String requestStatus = "Approved";


        if (activityName != null && !activityName.isEmpty() && activityStatus != null && !activityStatus.isEmpty()) {
            return searchVillageActivityByNameAndActivityStatusAndRequestStatusAndVillageId(activityName,activityStatus,requestStatus,villageId);
        }
        if (activityName != null && !activityName.isEmpty()) {
            return searchVillageActivityByNameAndRequestStatusAndVillageId(activityName,requestStatus,villageId);
        }
        if (activityType != null && !activityType.equalsIgnoreCase("All") && !activityType.isEmpty()
                && activityStatus != null && !activityStatus.isEmpty()) {
            return searchVillageActivityByActivityTypeAndActivityStatusAndRequestStatusAndVillageId(activityType,activityStatus,requestStatus,villageId);
        }
        if (activityType != null && !activityType.equalsIgnoreCase("All") && !activityType.isEmpty()) {
            return searchVillageActivityByActivityTypeAndRequestStatusAndVillageId(activityType,requestStatus,villageId);
        }
        if (activityStatus != null && !activityStatus.isEmpty()) {
            return searchVillageActivityByActivityStatusAndRequestStatusAndVillageId(activityStatus,requestStatus,villageId);
        }

        return searchVillageActivityByRequestStatusAndVillageId(requestStatus,villageId);
    }

    @Override
    public List<Activity> getAllFilteredRejectedActivitiesForVillageAdmin(String activityName, String activityType, Long villageId) {
        List<Activity> activities;
        String requestStatus = "Rejected";

        if (activityName != null && !activityName.isEmpty()) {
            return searchVillageActivityByNameAndRequestStatusAndVillageId(activityName,requestStatus,villageId);
        }
        if (activityType != null && !activityType.equalsIgnoreCase("All") && !activityType.isEmpty()) {
            return searchVillageActivityByActivityTypeAndRequestStatusAndVillageId(activityType,requestStatus,villageId);
        }
        return searchVillageActivityByRequestStatusAndVillageId(requestStatus,villageId);
    }

    @Override
    public List<Activity> getActivitiesByVillageId(Long villageId) {
        return activityRepository.findByVillage_VillageId(villageId);
    }

    @Override
    public List<Activity> getApprovedActivitiesByVillageId(Long villageId) {
        String status = "Approved";
        return activityRepository.findByVillage_VillageIdAndRequestStatusOrderByActivityNameAsc(villageId , status);
    }

    @Override
    public String updateActivity(ActivityDto activityDto) {
        Optional<Activity> optionalActivity = activityRepository.findById(activityDto.getActivityId());
        if (optionalActivity.isPresent()){
            Activity activity = optionalActivity.get();

            activity.setActivityName(activityDto.getActivityName());
            activity.setActivityPlace(activityDto.getActivityPlace());
            activity.setDescription(activityDto.getDescription());
            activity.setDuration(activityDto.getDuration());
            activity.setPrice(activityDto.getPrice());

            if ("seasonal".equalsIgnoreCase(activityDto.getActivityType()) ||
                    "permanent".equalsIgnoreCase(activityDto.getActivityType()) ||
                    "weatherDependent".equalsIgnoreCase(activityDto.getActivityType())){
                List<String> selectedDays = activityDto.getAvailableDays();
                if (selectedDays == null || selectedDays.isEmpty()){
                    selectedDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
                    activity.setAvailableDays(selectedDays);
                }
                activity.setAvailableDays(selectedDays);
                activity.setTimeSlots(activityDto.getTimeSlots());
                activity.setNoOfPeopleAllowedForSloted(activityDto.getNoOfPeopleAllowedForSloted());
            }

            if ("seasonal".equalsIgnoreCase(activityDto.getActivityType())){
                activity.setStartDate(activityDto.getStartDate());
                activity.setEndDate(activityDto.getEndDate());
            }

            if ("weatherDependent".equalsIgnoreCase(activityDto.getActivityType())){
                if (activityDto.getAlternativeActivityId() != null && activityDto.getAlternativeActivityId() != 0){
                    Optional<Activity> optionalActivity1 = activityRepository.findById(activityDto.getAlternativeActivityId());
                    if (optionalActivity1.isPresent()){
                        Activity altAct = optionalActivity1.get();
                        activity.setAlternativeActivity(altAct);
                    }
                }
                else {
                    activity.setAlternativeActivity(null);
                }
            }

            if ("realTimeEvent".equalsIgnoreCase(activityDto.getActivityType())){
                activity.setEventDateTime(activityDto.getEventDateTime());
                activity.setNoOfPeopleAllowedForDateOnly(activityDto.getNoOfPeopleAllowedForDateOnly());
            }

            activityRepository.save(activity);
            return "success";
        }
        return "activity not found";
    }

    @Override
    public boolean updateActivityImage(MultipartFile newImage, String oldImageName, Long activityId) {
        Optional<Activity> optionalActivity = activityRepository.findById(activityId);
        if (optionalActivity.isEmpty()){
            return false;
        }
        Activity activity = optionalActivity.get();
        List<String> imageNames = activity.getActivityPhotosNames();

        int index = imageNames.indexOf(oldImageName);
        if (index == -1){ return false; }

        File oldFile = new File("src/main/resources/static/uploads/activity/" + oldImageName);
        if (oldFile.exists()){ oldFile.delete(); }

        String uploadDir = "src/main/resources/static/uploads/activity/";
        String newImageName = saveSingleFile(newImage,uploadDir);
        if (newImageName == null){ return false; }

        imageNames.set(index,newImageName);
        activity.setActivityPhotosNames(imageNames);
        activityRepository.save(activity);

        return true;
    }

    @Override
    public boolean addMoreImagesToActivity(Long activityId, List<MultipartFile> newImages) {
        if (newImages == null || newImages.isEmpty()) return false;

        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) return false;

        List<String> existingImages = activity.getActivityPhotosNames();// Assuming this is your image list
        int currentCount = existingImages != null ? existingImages.size() : 0;
        int availableSlots = 5 - currentCount;

        if (newImages.size() > availableSlots) {
            return false; // Exceeds max limit
        }

        List<String> savedFiles = saveFile(newImages);
        if (savedFiles.isEmpty()) return false;

        existingImages.addAll(savedFiles);
        activity.setActivityPhotosNames(existingImages);
        activityRepository.save(activity);
        return true;
    }

    @Override
    public boolean deleteImageFromActivity(Long activityId, String imageName) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) return false;

        List<String> images = activity.getActivityPhotosNames();
        if (images == null || !images.remove(imageName)) return false;

        Path imagePath = Paths.get("src/main/resources/static/uploads/activity/", imageName);
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            return false;
        }

        activity.setActivityPhotosNames(images);
        activityRepository.save(activity);
        return true;
    }


    @Override
    public List<ActivityDto> getApprovedActivitiesByVillageName(String villageName) {
        String status = "Approved";
        String activityStatus = "Available";
        List<Activity> activities = activityRepository.findByVillage_VillageNameIgnoreCaseAndRequestStatusIgnoreCaseAndActivityStatusIgnoreCaseOrderByActivityNameAsc(villageName , status ,activityStatus);
        return activities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityDto> searchApprovedActivitiesByVillageNameAndMaxPrice(String villageName, double maxPrice) {
        String status = "Approved";
        String activityStatus = "Available";
            List<Activity> activities = activityRepository.findByVillage_VillageNameIgnoreCaseAndActivityStatusIgnoreCaseAndPriceLessThanEqualAndRequestStatusIgnoreCaseOrderByActivityNameAsc(villageName,activityStatus, maxPrice , status);
            return activities.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
    }

    @Override
    public List<Activity> getActivityByRequestStatusAndActivityStatus() {
        return activityRepository.findByRequestStatusIgnoreCaseAndActivityStatusIgnoreCase("Approved", "Available");
    }

    @Override
    public ActivityDto convertToDto(Activity activity) {
        ActivityDto dto = new ActivityDto();
        dto.setActivityId(activity.getActivityId());
        dto.setActivityName(activity.getActivityName());
        dto.setPrice(activity.getPrice());
        // Optional: map other fields as needed
        return dto;
    }

    @Override
    public List<Activity> filterActivities(String activityType, String villageName, String region) {
        return activityRepository.findByFilters(activityType, villageName, region);
    }

}

