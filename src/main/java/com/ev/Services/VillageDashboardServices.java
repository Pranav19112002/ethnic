package com.ev.Services;

import com.ev.Repository.ActivityBookingRepository;
import com.ev.Repository.ActivityRepository;
import com.ev.Repository.VillageStayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VillageDashboardServices {

    @Autowired
    public ActivityBookingRepository activityBookingRepository;

    @Autowired
    public ActivityRepository activityRepository;

    @Autowired
    public VillageStayRepository villageStayRepository;

    private String formatCount(long count) {
        if (count >= 1_000_000) {
            return String.format("%.1fm", count / 1_000_000.0);
        } else if (count >= 1_000) {
            return String.format("%.1fk", count / 1_000.0);
        } else {
            return String.valueOf(count);
        }
    }

    private String formatCurrency(double amount) {
        if (amount >= 1_000_000) return String.format("₹%.1fM", amount / 1_000_000);
        if (amount >= 1_000) return String.format("₹%.1fk", amount / 1_000);
        return String.format("₹%.0f", amount);
    }

    public String getFormattedConfirmedBookings(Long villageId){
        long confirmedBookings = activityBookingRepository.countByVillage_VillageIdAndStatusIgnoreCase(villageId,"confirmed");
        return formatCount(confirmedBookings);
    }
    public String getFormattedCancelledBookings(Long villageId){
        long cancelledBookings = activityBookingRepository.countByVillage_VillageIdAndStatusIgnoreCase(villageId,"cancelled");
        return formatCount(cancelledBookings);
    }
    public String getFormattedEndedBookings(Long villageId){
        long endedBookings = activityBookingRepository.countByVillage_VillageIdAndStatusIgnoreCase(villageId,"ended");
        return formatCount(endedBookings);
    }

    public String getFormattedRevenue(Long villageId) {
        Double total = activityBookingRepository.sumAmountPaidByVillageId(villageId);
        if (total == null) total = 0.0;
        return formatCurrency(total);
    }

    public String getFormattedAvailableActivityCount(Long villageId) {
        long count = activityRepository.countByVillage_VillageIdAndActivityStatusIgnoreCaseAndRequestStatusIgnoreCase(villageId,"Available","Approved");
        return formatCount(count);
    }

    public String getFormattedActiveVillageStayCount(Long villageId) {
        long count = villageStayRepository.countByVillage_VillageIdAndIsActiveTrue(villageId);
        return formatCount(count);
    }

    public String getFormattedAllActivitiesCount(Long villageId) {
        long count = activityRepository.countByVillage_VillageIdAndRequestStatusIgnoreCase(villageId , "Approved");
        return formatCount(count);
    }

    public String getFormattedAllStays(Long villageId) {
        long count = villageStayRepository.countByVillage_VillageId(villageId);
        return formatCount(count);
    }
}
