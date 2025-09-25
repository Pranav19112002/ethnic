package com.ev.Repository;

import com.ev.Model.Activity;
import com.ev.Model.ActivityBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ActivityBookingRepository extends JpaRepository<ActivityBooking , Long> {

    @Query("SELECT COALESCE(SUM(b.numberOfPeople), 0) FROM ActivityBooking b " +
            "WHERE b.activity.activityId = :activityId AND b.bookingDate = :date AND b.status = 'confirmed'")
    int countConfirmedPeopleByActivityIdAndDate(@Param("activityId") Long activityId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(b.numberOfPeople), 0) FROM ActivityBooking b " +
            "WHERE b.activity.activityId = :activityId AND b.bookingDate = :date AND b.timeSlot = :slot AND b.status = 'confirmed'")
    int countConfirmedPeopleByActivityIdDateAndSlot(@Param("activityId") Long activityId, @Param("date") LocalDate date, @Param("slot") LocalTime slot);

    List<ActivityBooking> findByUser_UserIdOrderByBookingIdDesc(Long userId);
    List<ActivityBooking> findByUser_UserIdAndStatusIgnoreCaseAndPaymentStatusIgnoreCaseOrderByBookingIdDesc(Long userId , String status ,String paymentStatus);
    List<ActivityBooking> findByUser_UserIdAndStatusInOrderByExpiredOrCancelledAtDesc(Long userId, List<String> statuses);
    List<ActivityBooking> findByUser_UserIdAndStatusIgnoreCaseOrderByBookingIdDesc(Long userId , String status);

    List<ActivityBooking> findByPaymentStatusIgnoreCaseAndStatusIgnoreCaseAndExpiresAtBefore(String paymentStatus,String status, LocalDateTime time);

    List<ActivityBooking> findByBookingDateBeforeAndStatusIgnoreCase(LocalDate date , String status);

    List<ActivityBooking> findByUser_UserIdAndStatusIgnoreCaseInOrderByBookingIdDesc(Long userId, List<String> statusList);


    @Query("SELECT b FROM ActivityBooking b WHERE b.village.villageId = :villageId AND LOWER(b.status) IN :statuses")
    List<ActivityBooking> findByVillage_VillageIdAndStatusInIgnoreCase(@Param("villageId") Long villageId,
                                                                       @Param("statuses") List<String> statuses);

    @Query("SELECT b FROM ActivityBooking b WHERE b.activity.activityId = :activityId AND LOWER(b.status) IN :statuses")
    List<ActivityBooking> findByActivity_ActivityIdAndStatusInIgnoreCase(@Param("activityId") Long activityId,
                                                                       @Param("statuses") List<String> statuses);

    @Query("SELECT b FROM ActivityBooking b WHERE b.activity = :activity AND LOWER(b.status) IN :statuses")
    List<ActivityBooking> findByActivityAndStatusInIgnoreCase(@Param("activity") Activity activity,
                                                                         @Param("statuses") List<String> statuses);


    List<ActivityBooking> findByActivity_ActivityIdAndStatusIgnoreCase(Long activityId, String status);

    List<ActivityBooking> findByActivityAndStatusIgnoreCase(Activity activity , String status);

    List<ActivityBooking>findByStatusIgnoreCaseOrderByBookingIdDesc(String status);
    List<ActivityBooking>findByStatusIgnoreCaseInOrderByExpiredOrCancelledAtDesc(List<String> status);

    long countByVillage_VillageIdAndStatusIgnoreCase(Long villageId, String status);

    @Query("SELECT SUM(ab.amountPaid) FROM ActivityBooking ab WHERE ab.village.id = :villageId AND ab.paid = true")
    Double sumAmountPaidByVillageId(@Param("villageId") Long villageId);

    @Query("SELECT COALESCE(SUM(ab.numberOfPeople), 0) FROM ActivityBooking ab " +
            "WHERE ab.activity.activityId = :activityId " +
            "AND ab.bookingDate = :date " +
            "AND ab.timeSlot = :slot " +
            "AND ab.status = 'confirmed'")
    int countConfirmedPeopleForSlot(@Param("activityId") Long activityId,
                                    @Param("date") LocalDate date,
                                    @Param("slot") LocalTime slot);

    @Query("SELECT COALESCE(SUM(ab.numberOfPeople), 0) FROM ActivityBooking ab " +
            "WHERE ab.activity.activityId = :activityId " +
            "AND ab.bookingDate = :date " +
            "AND ab.status = 'confirmed'")
    int countConfirmedPeople(@Param("activityId") Long activityId,
                             @Param("date") LocalDate date);

    @Query("SELECT b FROM ActivityBooking b WHERE b.activity.activityId = :activityId AND b.bookingDate = :date AND b.timeSlot = :slot AND b.status = 'pending'")
    List<ActivityBooking> findPendingBookingsByActivityIdDateAndSlot(Long activityId, LocalDate date, LocalTime slot);

}
