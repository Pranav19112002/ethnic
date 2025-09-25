package com.ev.Services;


import com.ev.Model.*;
import com.ev.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VillageServices implements VillageService {

    @Autowired
    private VillageRepository villageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private NotificationServices notificationServices;

    @Autowired
    private TempVillageRepository tempVillageRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String uploadDir = "uploads/village/";

    @Override
    public String generateCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            captcha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return captcha.toString();
    }

    @Override
    public String registerVillage(VillageDto villageDto) {
        LocalDateTime regDate = LocalDateTime.now();


        if (isVillageNameTaken(villageDto.getVillageName())) {
            return "Village name already exists!";
        }

        if ((isVillageRegNumberTaken(villageDto.getVillageRegNumber()))) {
            return "Village Register Number is already exists!";
        }

        if (isEmailTaken(villageDto.getVillageEmail())) {
            return "Village Email already exists!";
        }

        if (isBankAccountNumberTaken(villageDto.getBankAccountNumber())) {
            return "Bank Account number already exists!";
        }

        Village village = new Village();
        village.setRegisteredDate(regDate);
        village.setVillageName(villageDto.getVillageName());
        village.setVillageLocation(villageDto.getVillageLocation());
        village.setVillageRegNumber(villageDto.getVillageRegNumber());
        village.setVillageAuthorityName(villageDto.getVillageAuthorityName());
        village.setOffDocFileName(villageDto.getOfficialDocPath());
        village.setOffStampOrSealFileName(villageDto.getOffStampOrSealPath());
        village.setVillagePhotos(villageDto.getVillagePhotoPaths());
        village.setVillageDescription(villageDto.getVillageDescription());
        village.setVillageRepresentativeName(villageDto.getLegalRepresentativeName());
        village.setRepresentativePosition(villageDto.getRepresentativePosition());
        village.setOfficialIdNumber(villageDto.getOfficialIdNumber());
        village.setVillageEmail(villageDto.getVillageEmail());
        village.setContactPersonName(villageDto.getContactPersonName());
        village.setContactEmail(villageDto.getContactEmail());
        village.setContactNo(villageDto.getContactNo());
        village.setAltContactNo(villageDto.getAltContactNo());
        village.setBankName(villageDto.getBankName());
        village.setBankAccountNumber(villageDto.getBankAccountNumber());
        village.setTin(villageDto.getTaxInfoNumber());
        village.setVillageType(villageDto.getVillageType());
        village.setLatitude(villageDto.getLatitude());
        village.setLongitude(villageDto.getLongitude());
        village.setVillagePassword(passwordEncoder.encode(villageDto.getNewPassword()));
        villageRepository.save(village);
        return "success";
    }

    @Override
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String fileName = System.currentTimeMillis() + "_" + sanitizedFilename;
        try {
            String uploadDir = "src/main/resources/static/uploads/village/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("File upload error: " + ex.getMessage());
        }
        return fileName;
    }

    @Override
    public List<String> saveMultipleFiles(List<MultipartFile> files) {

        List<String> filePaths = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return filePaths;
        }
        try {
            String uploadDir = "src/main/resources/static/uploads/village/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String sanitizedFilename = sanitizeFilename(originalFilename);
                    String fileName = System.currentTimeMillis() + "_" + sanitizedFilename;
                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    filePaths.add(fileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving files: " + e.getMessage());
        }
        return filePaths;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }


    @Override
    public boolean isVillageNameTaken(String villageName) {
        return villageRepository.findByVillageNameIgnoreCase(villageName).isPresent();
    }

    @Override
    public boolean isVillageRegNumberTaken(String villageRegNumber) {
        return villageRepository.findByVillageRegNumberIgnoreCase(villageRegNumber).isPresent();
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.findByUserEmail(email).isPresent()
                || villageRepository.findByVillageEmail(email).isPresent();
    }

    @Override
    public boolean isBankAccountNumberTaken(String bankAccountNumber) {
        return villageRepository.findByBankAccountNumber(bankAccountNumber).isPresent();
    }

    @Override
    public Village checkVillageExists(String villageEmail, String password) {
        Optional<Village> optionalVillage = villageRepository.findByVillageEmailAndVillageStatusIgnoreCase(villageEmail, "Approved");
        if (optionalVillage.isPresent()) {
            Village village = optionalVillage.get();
            if (passwordEncoder.matches(password, village.getVillagePassword())) {
                return village;
            }
        }
        return null;
    }

    @Override
    public Village getVillageById(Long villageId) {
        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        return optionalVillage.orElse(null);
    }

    @Override
    public Village getVillageByEmail(String email) {
        Optional<Village> optionalVillage = villageRepository.findByVillageEmail(email);
        return optionalVillage.orElse(null);
    }

    @Override
    public List<Village> getVillageRequests() {
        return villageRepository.findByVillageStatusOrderByVillageIdDesc("request-send");
    }

    @Override
    public List<Village> searchAllVillagesByVillageNameAndVillageStatus(String villageName, String villageStatus) {
        return villageRepository.findByVillageNameStartsWithIgnoreCaseAndVillageStatusIgnoreCaseOrderByVillageIdDesc(villageName, villageStatus);
    }

    @Override
    public List<Village> searchAllVillagesByVillageName(String villageName) {
        return villageRepository.findByVillageNameStartsWithIgnoreCaseOrderByVillageIdDesc(villageName);
    }

    @Override
    public List<Village> searchAllVillagesByCurrentStatusAndVillageStatus(String currentStatus, String villageStatus) {
        return villageRepository.findByVillageCurrentStatusIgnoreCaseAndVillageStatusIgnoreCaseOrderByVillageIdDesc(currentStatus, villageStatus);
    }

    @Override
    public List<Village> searchAllVillagesByCurrentStatus(String currentStatus) {
        return villageRepository.findByVillageCurrentStatusIgnoreCaseOrderByVillageIdDesc(currentStatus);
    }

    @Override
    public List<Village> searchAllVillagesByVillageStatus(String villageStatus) {
        return villageRepository.findByVillageStatusOrderByVillageIdDesc(villageStatus);
    }

    @Override
    public List<Village> getAllFilteredVillagesForAdmin(
            String villageName, String currentStatus, String villageStatus) {

        List<Village> villages;

        if (villageName != null && !villageName.isEmpty() && villageStatus != null && !villageStatus.isEmpty()) {
            return searchAllVillagesByVillageNameAndVillageStatus(villageName, villageStatus);
        }
        if (villageName != null && !villageName.isEmpty()) {
            return searchAllVillagesByVillageName(villageName);
        }
        if (currentStatus != null && !currentStatus.equalsIgnoreCase("All") && !currentStatus.isEmpty()
                && villageStatus != null && !villageStatus.isEmpty()) {
            return searchAllVillagesByCurrentStatusAndVillageStatus(currentStatus, villageStatus);
        }
        if (currentStatus != null && !currentStatus.equalsIgnoreCase("All") && !currentStatus.isEmpty()) {
            return searchAllVillagesByCurrentStatus(currentStatus);
        }
        if (villageStatus != null && !villageStatus.isEmpty()) {
            return searchAllVillagesByVillageStatus(villageStatus);
        }
        return getAllVillages();
    }


    @Override
    public List<Village> getAllVillages() {
        return villageRepository.findAllByOrderByVillageIdDesc();
    }


    @Override
    public Village updateVillageStatus(Long villageId, String status) {
        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        if (optionalVillage.isPresent()) {
            Village village = optionalVillage.get();
            village.setVillageStatus(status);
            village.setApprovedOrRejectedAt(LocalDateTime.now());
            villageRepository.save(village);
            return village;
        }
        return null;
    }

    @Transactional
    public boolean updateVillageStatusAndCascade(Long villageId, String newStatus) {
        System.out.println("🔄 [CASCADE] Starting status update for villageId: " + villageId + " → " + newStatus);

        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        if (optionalVillage.isEmpty()) {
            System.out.println("❌ [CASCADE] Village not found for ID: " + villageId);
            return false;
        }

        Village village = optionalVillage.get();
        village.setVillageCurrentStatus(newStatus);
        villageRepository.save(village);
        System.out.println("✅ [CASCADE] Village status updated to: " + newStatus);

        List<Activity> activities = activityRepository.findByVillage_VillageId(villageId);
        System.out.println("📦 [CASCADE] Found " + activities.size() + " activities for village");

        for (Activity activity : activities) {
            if (newStatus.equalsIgnoreCase("unavailable") || newStatus.equalsIgnoreCase("Deactivated")) {
                activity.setActivityStatus("Unavailable");
                System.out.println("⚠️ [ACTIVITY] Marked as unavailable → " + activity.getActivityName());
            } else if (newStatus.equalsIgnoreCase("available")) {
                if (activity.getActivityStatus().equalsIgnoreCase("unavailable")) {
                    activity.setActivityStatus("Available");
                    System.out.println("✅ [ACTIVITY] Restored to available → " + activity.getActivityName());
                }
            }
        }
        activityRepository.saveAll(activities);
        System.out.println("📁 [ACTIVITY] Activity statuses saved");

        if (newStatus.equalsIgnoreCase("unavailable") || newStatus.equalsIgnoreCase("Deactivated")) {
            List<ActivityBooking> bookings = activityBookingRepository
                    .findByVillage_VillageIdAndStatusInIgnoreCase(villageId, List.of("confirmed", "pending"));
            System.out.println("📦 [BOOKINGS] Found " + bookings.size() + " bookings to cancel");

            for (ActivityBooking booking : bookings) {
                booking.setStatus("cancelled");
                booking.setExpiredOrCancelledAt(LocalDateTime.now());

                String reason = booking.getStatus().equalsIgnoreCase("pending")
                        ? "Village marked as " + newStatus + " before payment"
                        : "Village marked as " + newStatus;

                booking.setExpiredOrCancelReason(reason);
                activityBookingRepository.save(booking);

                System.out.println("❌ [BOOKING] Cancelled booking #" + booking.getBookingId() + " for activity: " + booking.getActivity().getActivityName());

                // Build tailored message
                StringBuilder msg = new StringBuilder("❌ Your booking for activity '");
                msg.append(booking.getActivity().getActivityName())
                        .append("' on ").append(booking.getBookingDate());

                if (booking.getTimeSlot() != null) {
                    msg.append(" at ").append(booking.getTimeSlot());
                }

                msg.append(" in village '").append(booking.getVillage().getVillageName())
                        .append("' has been cancelled due to village status: ").append(newStatus).append(".");

                if (booking.getStatus().equalsIgnoreCase("cancelled") && reason.contains("before payment")) {
                    msg.append(" You can try booking another activity or check back later.");
                } else {
                    msg.append(" Refund will be processed shortly. For assistance, contact ")
                            .append(booking.getVillage().getVillageEmail())
                            .append(" with your booking ID: ").append(booking.getBookingId()).append(".");
                }

                notificationServices.sendInboxMessageToUserVillageStatusChange(
                        booking.getUser().getUserId(),
                        msg.toString(),
                        booking.getActivity(),
                        booking,
                        booking.getVillage()
                );
                System.out.println("📨 [NOTIFY] Inbox message sent to userId: " + booking.getUser().getUserId());

                mailService.sendVillageStatusBasedEmail(
                        booking.getUser().getUserEmail(),
                        booking.getVillage().getVillageName(),
                        newStatus
                );
                System.out.println("📧 [EMAIL] Cancellation email sent to: " + booking.getUser().getUserEmail());
            }
        }

        System.out.println("✅ [CASCADE] Status update and cascade completed for villageId: " + villageId);
        return true;
    }



    @Override
    public String updateVillageProfileImage(MultipartFile profileImg, Long villageId) {
        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        String profileImageName = saveFile(profileImg);
        if (optionalVillage.isPresent()) {
            Village village = optionalVillage.get();
            village.setVillageProfileImage(profileImageName);
            villageRepository.save(village);
            return "success";
        }
        return "failed";
    }

    @Override
    public boolean updateVillageImage(Long villageId, String oldImageName, MultipartFile newImageFile) {
        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        if (optionalVillage.isEmpty()) {
            return false;
        }
        Village village = optionalVillage.get();
        List<String> imageNames = village.getVillagePhotos();

        int index = imageNames.indexOf(oldImageName);
        if (index == -1) {
            return false;
        }

        File oldFile = new File("src/main/resources/static/uploads/village/" + oldImageName);
        if (oldFile.exists()) {
            oldFile.delete();
        }
        String newImageName = saveFile(newImageFile);
        if (newImageName.isEmpty()) {
            return false;
        }
        imageNames.set(index, newImageName);
        village.setVillagePhotos(imageNames);
        villageRepository.save(village);
        return true;
    }

    @Override
    public String updateVillageDetails(VillageDto villageDto) {
        Optional<Village> optionalVillage = villageRepository.findById(villageDto.getVillageId());
        if (optionalVillage.isPresent()) {
            Village village = optionalVillage.get();
            village.setVillageName(villageDto.getVillageName());
            village.setVillageType(villageDto.getVillageType());
            village.setVillageDescription(villageDto.getVillageDescription());
            village.setContactEmail(villageDto.getContactEmail());
            village.setContactNo(villageDto.getContactNo());
            village.setAltContactNo(villageDto.getAltContactNo());
            villageRepository.save(village);
            return "success";
        }
        return "failed";
    }

    @Override
    public Map<Long, Village> getCurrentVillageMapForVillageUpdate(List<TempVillage> tempVillages) {
        List<Long> villageIds = tempVillages.stream()
                .filter(Objects::nonNull)
                .map(TempVillage::getVillageId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (villageIds.isEmpty()) {
            System.out.println("No valid village IDs found.");
            return Collections.emptyMap();
        }

        List<Village> villages = villageRepository.findAllById(villageIds);

        return villages.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Village::getVillageId, Function.identity()));
    }

    @Override
    @Transactional
    public Village approveVillageUpdateRequest(Long requestId, Map<String, String> updatePayload) {

        TempVillage tempVillage = tempVillageRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Update request not found"));

        Optional<Village> optionalVillage = villageRepository.findById(tempVillage.getVillageId());

        if (optionalVillage.isPresent()) {
            Village village = optionalVillage.get();

            boolean isUpdated = false;

            for (Map.Entry<String, String> entry : updatePayload.entrySet()) {
                String field = entry.getKey();
                String newValue = entry.getValue();

                if (newValue == null || newValue.trim().isEmpty()) {
                    continue;
                }

                switch (field) {
                    case "name":
                        if (!newValue.equals(village.getVillageName())) {
                            village.setVillageName(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "location":
                        if (!newValue.equals(village.getVillageLocation())) {
                            village.setVillageLocation(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "regNo":
                        if (!newValue.equals(village.getVillageRegNumber())) {
                            village.setVillageRegNumber(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "authorityName":
                        if (!newValue.equals(village.getVillageAuthorityName())) {
                            village.setVillageAuthorityName(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "description":
                        if (!newValue.equals(village.getVillageDescription())) {
                            village.setVillageDescription(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "repName":
                        if (!newValue.equals(village.getVillageRepresentativeName())) {
                            village.setVillageRepresentativeName(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "repPosition":
                        if (!newValue.equals(village.getRepresentativePosition())) {
                            village.setRepresentativePosition(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "offIdNo":
                        if (!newValue.equals(village.getOfficialIdNumber())) {
                            village.setOfficialIdNumber(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "contactPersonName":
                        if (!newValue.equals(village.getContactPersonName())) {
                            village.setContactPersonName(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "contactEmail":
                        if (!newValue.equals(village.getContactEmail())) {
                            village.setContactEmail(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "contactNo":
                        if (!newValue.equals(village.getContactNo())) {
                            village.setContactNo(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "altContactNo":
                        if (!newValue.equals(village.getAltContactNo())) {
                            village.setAltContactNo(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "bankName":
                        if (!newValue.equals(village.getBankName())) {
                            village.setBankName(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "accNo":
                        if (!newValue.equals(village.getBankAccountNumber())) {
                            village.setBankAccountNumber(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "tin":
                        if (!newValue.equals(village.getTin())) {
                            village.setTin(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "type":
                        if (!newValue.equals(village.getVillageType())) {
                            village.setVillageType(newValue);
                            isUpdated = true;
                        }
                        break;

                    case "stamporseal":
                        if (!newValue.equals(village.getOffStampOrSealFileName())) {
                            village.setOffStampOrSealFileName(newValue);
                            isUpdated = true;
                        }
                        break;
                    case "offDocfile":
                        if (!newValue.equals(village.getOffDocFileName())) {
                            village.setOffDocFileName(newValue);
                            isUpdated = true;
                        }
                        break;
                    default:
                        // Unknown field - ignore or log
                }
            }

            if (isUpdated) {
                villageRepository.save(village);
                tempVillage.setStatus("Approved");
                tempVillage.setReviewedAt(LocalDate.now());
                tempVillageRepository.save(tempVillage);
                return village;
            } else {
                return village;
            }

        }
        return null;
    }

    @Override
    public TempVillage rejectVillageUpdateRequest(Long id, String message) {
        Optional<TempVillage> optionalTempVillage = tempVillageRepository.findById(id);
        if (optionalTempVillage.isPresent()) {
            TempVillage tempVillage = optionalTempVillage.get();
            tempVillage.setAdminComment(message);
            tempVillage.setReviewedAt(LocalDate.now());
            tempVillage.setStatus("Rejected");
            tempVillageRepository.save(tempVillage);
            return tempVillage;
        }
        return null;
    }

    @Override
    public String addMoreVillageImages(Long villageId, List<MultipartFile> files) {

        int maxCount = 5;

        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        if (optionalVillage.isPresent()) {
            Village village = optionalVillage.get();

            // Get existing images
            List<String> existingImages = new ArrayList<>();
            if (village.getVillagePhotos() != null) {
                existingImages = new ArrayList<>(village.getVillagePhotos());
            }

            int currentCount = existingImages.size();
            int newCount = files != null ? files.size() : 0;

            int remainingSpace = maxCount - currentCount;

            if (currentCount >= maxCount || currentCount + newCount > maxCount) {
                return "Error=Max Limit Reached.You can Add" + remainingSpace + "More Images";
            }
            List<String> newFileNames = saveMultipleFiles(files);
            existingImages.addAll(newFileNames);
            village.setVillagePhotos(existingImages);
            villageRepository.save(village);

            return "Success=Images Added SuccessFully";
        }

        return "Error=Village Id " + villageId + "not Found";
    }

    @Override
    public String deleteVillageProfileImage(Long villageId) {
        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        if (optionalVillage.isEmpty()) {
            return "Error=Village not found.";
        }

        Village village = optionalVillage.get();
        String image = village.getVillageProfileImage();

        if (image == null || image.isBlank()) {
            return "Error=No profile image to delete.";
        }

        Path imagePath = Paths.get("src/main/resources/static/uploads/village/", image)
                .toAbsolutePath()
                .normalize();

        System.out.println("Attempting to delete image: " + imagePath);

        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            return "Error=Failed to delete image from storage.";
        }
        village.setVillageProfileImage(null);
        villageRepository.save(village);

        return "Success=Profile image deleted successfully.";
    }

    @Override
    public boolean deleteImageFromVillage(Long villageId, String imageName) {
        Village village = villageRepository.findById(villageId).orElse(null);
        if (village == null) return false;

        List<String> images = village.getVillagePhotos();
        if (images == null || !images.remove(imageName)) return false;

        Path imagePath = Paths.get("src/main/resources/static/uploads/village/", imageName);
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            return false;
        }

        village.setVillagePhotos(images);
        villageRepository.save(village);
        return true;
    }

    @Override
    public List<String> findVillageLocationsContaining(String query) {
        return villageRepository.findDistinctLocationsContaining(query.toLowerCase());
    }

    @Override
    public List<Village> findAvailableVillagesByLocationAndVillageName(String location, String villageName) {
        String villageStatus = "Approved";
        String villageCurrentStatus = "Available";

        if (location != null && villageName != null) {
            return villageRepository.findByVillageLocationStartingWithIgnoreCaseAndVillageNameStartingWithIgnoreCaseAndVillageStatusAndVillageCurrentStatusOrderByVillageNameAsc(location, villageName, villageStatus, villageCurrentStatus);
        } else if (location != null) {
            return villageRepository.findByVillageLocationStartingWithIgnoreCaseAndVillageStatusAndVillageCurrentStatusOrderByVillageNameAsc(location, villageStatus, villageCurrentStatus);
        } else if (villageName != null) {
            return villageRepository.findByVillageNameStartingWithIgnoreCaseAndVillageStatusAndVillageCurrentStatusOrderByVillageNameAsc(villageName, villageStatus, villageCurrentStatus);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String updateVillageEmail(VillageDto villageDto) {
        Optional<Village> optionalVillage = villageRepository.findById(villageDto.getVillageId());
        if (optionalVillage.isEmpty()) {
            return "Error=Village not found.";
        }
        Village village = optionalVillage.get();
        village.setVillageEmail(villageDto.getNewEmail());
        villageRepository.save(village);
        return "success";
    }

    @Override
    public String updatePassword(Long villageId, String newPassword) {
        Optional<Village> optionalVillage = villageRepository.findById(villageId);
        if (optionalVillage.isEmpty()) {
            return "Error=Village not found.";
        }
        Village village = optionalVillage.get();
        village.setVillagePassword(passwordEncoder.encode(newPassword));
        villageRepository.save(village);
        return "success";
    }
}

