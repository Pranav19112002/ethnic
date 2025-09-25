package com.ev.Repository;

import com.ev.Model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity , Long> {

    boolean existsByActivityNameAndVillage_VillageId(String activityName , Long villageId);

    List<Activity> findByVillage_VillageIdOrderByActivityIdDesc(Long villageId);
    List<Activity> findByVillage_VillageIdAndRequestStatusIgnoreCaseOrderByActivityIdDesc(Long villageId , String requestStatus);

    @Query("SELECT DISTINCT a.activityType FROM Activity  a")
    List<String> findAllVillageTypes();

    @Query("SELECT DISTINCT a.requestStatus FROM Activity  a")
    List<String> findAllRequestStatuses();

    List<Activity> findByActivityNameContainingIgnoreCaseAndVillage_VillageId(String searchKeyword , Long villageId);
    List<Activity> findByActivityNameStartsWithIgnoreCaseAndVillage_VillageIdAndRequestStatusIgnoreCaseOrderByActivityIdDesc(String searchKeyword , Long villageId , String requestStatus);
    List<Activity> findByActivityNameStartsWithIgnoreCaseAndRequestStatus(String searchKeyword , String status);
    List<Activity> findByActivityTypeAndVillage_VillageIdAndRequestStatusIgnoreCaseOrderByActivityIdDesc(String villageType , Long villageId ,String requestStatus);
    List<Activity> findByActivityTypeAndRequestStatusOrderByActivityIdDesc(String villageType , String status);
    List<Activity> findByRequestStatusAndVillage_VillageIdOrderByActivityIdDesc(String requestStatus , Long villageId);
    List<Activity> findByRequestStatusOrderByActivityIdDesc(String status);
    List<Activity> findAllByOrderByActivityIdDesc();
    List<Activity> findByActivityNameStartsWithIgnoreCaseOrderByActivityIdDesc(String activityName);
    List<Activity> findByActivityTypeOrderByActivityIdDesc(String activityType);
    List<Activity> findByVillage_VillageId(Long villageId);
    List<Activity> findByVillage_VillageIdAndRequestStatusOrderByActivityNameAsc(Long villageId , String requestStatus);
    List<Activity> findByActivityNameStartsWithIgnoreCaseAndRequestStatusOrderByActivityIdDesc(String activityName ,String requestStatus);
    List<Activity> findByActivityTypeIgnoreCaseAndRequestStatusOrderByActivityIdDesc(String activityType ,String requestStatus);
    List<Activity> findByVillage_VillageIdAndRequestStatusOrderByActivityIdDesc(Long villageId ,String requestStatus);
        List<Activity> findByVillage_VillageIdAndActivityNameStartsWithIgnoreCaseAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(Long villageId,String activityName,String activityStatus,String requestStatus);
    List<Activity> findByVillage_VillageIdAndActivityNameStartsWithIgnoreCaseAndRequestStatusIgnoreCase(Long villageId,String activityName,String requestStatus);
    List<Activity> findByVillage_VillageIdAndActivityTypeIgnoreCaseAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(Long villageId,String activityType,String activityStatus,String requestStatus);
    List<Activity> findByVillage_VillageIdAndActivityTypeIgnoreCaseAndRequestStatusIgnoreCase(Long villageId,String activityType,String requestStatus);
    List<Activity> findByVillage_VillageIdAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(Long villageId,String activityStatus,String requestStatus);
    List<Activity> findByVillage_VillageIdAndRequestStatusIgnoreCase(Long villageId,String requestStatus);

    List<Activity> findByVillage_VillageNameIgnoreCaseAndRequestStatusIgnoreCaseAndActivityStatusIgnoreCaseOrderByActivityNameAsc(String villageName , String requestStatus ,String activityStatus);
    List<Activity> findByVillage_VillageNameIgnoreCaseAndActivityStatusIgnoreCaseAndPriceLessThanEqualAndRequestStatusIgnoreCaseOrderByActivityNameAsc(String villageName,String activityStatus, Double maxPrice , String requestStatus);

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.activityStatus = 'Available'")
    long countAvailableActivities();

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.activityStatus = 'Unavailable'")
    long countUnavailableActivities();

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.activityStatus = 'Ended'")
    long countEndedActivities();

    long countByVillage_VillageIdAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(Long villageId , String status , String requestStatus);
    long countByVillage_VillageIdAndRequestStatusIgnoreCase(Long villageId , String requestStatus);

    List<Activity> findByRequestStatusIgnoreCaseAndActivityStatusIgnoreCase(String requestStatus , String activityStatus);

    @Query("SELECT a FROM Activity a WHERE " +
            "(:activityName IS NULL OR LOWER(a.activityName) LIKE LOWER(CONCAT('%', :activityName, '%'))) AND " +
            "(:villageName IS NULL OR LOWER(a.village.villageName) LIKE LOWER(CONCAT('%', :villageName, '%'))) AND " +
            "(:region IS NULL OR LOWER(a.village.villageLocation) LIKE LOWER(CONCAT('%', :region, '%')))")
    List<Activity> findByFilters(
            @Param("activityName") String activityName,
            @Param("villageName") String villageName,
            @Param("region") String region
    );


}

