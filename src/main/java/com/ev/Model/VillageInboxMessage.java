package com.ev.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class VillageInboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", nullable = false)
    private Village village;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VillageNotificationType type; // e.g. ADMIN_APPROVAL, USER_BOOKING, WEATHER_UPDATE

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    private boolean isRead = false;

    // Optional: link to activity or booking if relevant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private ActivityBooking activityBooking;

    // Getters and setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public VillageNotificationType getType() {
        return type;
    }

    public void setType(VillageNotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public ActivityBooking getActivityBooking() {
        return activityBooking;
    }

    public void setActivityBooking(ActivityBooking activityBooking) {
        this.activityBooking = activityBooking;
    }
}
