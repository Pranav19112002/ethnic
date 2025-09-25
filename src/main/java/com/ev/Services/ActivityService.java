package com.ev.Services;

import com.ev.Model.Activity;
import com.ev.Model.ActivityDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;


public interface ActivityService {

    boolean isActivityNameTaken(String activityName , Long villageId);

    Activity getActivityById(Long activityId);

    Activity saveActivity(ActivityDto activityDto);


    List<String> saveFile(List<MultipartFile> files);
    String saveSingleFile(MultipartFile file, String uploadDir );
    List<Activity> getAllActivitiesByVillageId(Long villageId);
    List<Activity> getAllPendingActivitiesByVillageId(Long villageId);
    List<Activity> getAllActivities();
    List<String> getAllActivityTypes();
    List<String> getAllRequestStatuses();
    List<Activity> getAllActivityRequests();
    List<Activity> searchActivitiesByName(String searchKeyword, Long villageId);
    List<Activity> searchPendingActivitiesByName(String searchKeyword, Long villageId);
    List<Activity> searchActivitiesRequestsByName(String searchKeyword);
    List<Activity> searchAllActivitiesByName(String searchKeyword);
    List<Activity> searchAllActivitiesByVillageId(Long villageId);
    List<Activity> searchActivitiesByType(String villageType , Long villageId);
    List<Activity> searchActivitiesRequestsByType(String activityType);
    List<Activity> searchAllActivitiesByType(String activityType);
    List<Activity> searchAllActivitiesByNameAndRequestStatus(String activityName , String requestStatus);
    List<Activity> searchAllActivitiesByTypeAndRequestStatus(String activityType , String requestStatus);
    List<Activity> searchAllActivitiesByVillageIdAndRequestStatus(Long villageId , String requestStatus);
    List<Activity> searchAllActivitiesByRequestStatus(String requestStatus);
    List<Activity> searchActivitiesByReqStatus(String requestStatus , Long villageId);
    List<Activity> getAllFilteredActivitiesForAdmin(String activityName , Long villageId , String activityType , String requestStatus );

    boolean updateActivityStatus(Long activityId , String status);
    boolean updateRequestStatus(Long activityId , String status , Long villageId);
    boolean deleteActivityById(long activityId);

    List<Activity> searchVillageActivityByNameAndActivityStatusAndRequestStatusAndVillageId(String activityName,String activityStatus,String requestStatus,Long villageId);
    List<Activity> searchVillageActivityByNameAndRequestStatusAndVillageId(String activityName,String requestStatus,Long villageId);
    List<Activity> searchVillageActivityByActivityTypeAndActivityStatusAndRequestStatusAndVillageId(String activityType,String activityStatus,String requestStatus,Long villageId);
    List<Activity> searchVillageActivityByActivityTypeAndRequestStatusAndVillageId(String activityType,String requestStatus,Long villageId);
    List<Activity> searchVillageActivityByActivityStatusAndRequestStatusAndVillageId(String activityStatus,String requestStatus,Long villageId);
    List<Activity> searchVillageActivityByRequestStatusAndVillageId(String requestStatus,Long villageId);
    List<Activity> getAllFilteredActivitiesForVillageAdmin(String activityName,String activityType,String activityStatus,Long villageId);
    List<Activity> getAllFilteredRejectedActivitiesForVillageAdmin(String activityName,String activityType,Long villageId);
    List<Activity> getActivitiesByVillageId(Long villageId);
    List<Activity> getApprovedActivitiesByVillageId(Long villageId);
    String updateActivity(ActivityDto activityDto);
    boolean updateActivityImage(MultipartFile newImage , String oldImageName , Long activityId);
    boolean addMoreImagesToActivity(Long activityId, List<MultipartFile> newImages);
    boolean deleteImageFromActivity(Long activityId, String imageName);

    List<ActivityDto> getApprovedActivitiesByVillageName(String villageName);
    List<ActivityDto> searchApprovedActivitiesByVillageNameAndMaxPrice(String villageName, double maxPrice);

    List<Activity> getActivityByRequestStatusAndActivityStatus();

    ActivityDto convertToDto(Activity activity);

    List<Activity> filterActivities(String activityType, String villageName, String region);

}

