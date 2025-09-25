package com.ev.Services;

import com.ev.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VillageRepository villageRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private BookingsFeedbackRepository  bookingsFeedbackRepository;

    public String getFormattedUserCount() {
        long count = userRepository.count();
        return formatCount(count);
    }

    private String formatCount(long count) {
        if (count >= 1_000_000) {
            return String.format("%.1fm", count / 1_000_000.0);
        } else if (count >= 1_000) {
            return String.format("%.1fk", count / 1_000.0);
        } else {
            return String.valueOf(count);
        }
    }

    public String getFormattedVillageCount() {
        long count = villageRepository.count();
        return formatCount(count); // Reuse same format logic
    }

    public String getFormattedActivityCount() {
        long count = activityRepository.count();
        return formatCount(count); // Reuse your short-form logic
    }

    public String getFormattedBookingCount() {
        long count = activityBookingRepository.count();
        return formatCount(count); // Reuse your short-form logic
    }

    public String getFormattedStarCount() {
        long totalStars = bookingsFeedbackRepository.getTotalStarsReceived();
        return formatCount(totalStars); // Reuse your short-form logic
    }

    public String getFormattedAvailableCount() {
        return formatCount(activityRepository.countAvailableActivities());
    }

    public String getFormattedUnavailableCount() {
        return formatCount(activityRepository.countUnavailableActivities());
    }

    public String getFormattedEndedCount() {
        return formatCount(activityRepository.countEndedActivities());
    }


}
