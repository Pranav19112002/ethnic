package com.ev.Services;

import com.ev.Model.TempVillage;
import com.ev.Model.Village;
import com.ev.Model.VillageDto;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface VillageService {

    String generateCaptcha();

    String registerVillage(VillageDto villageDto);
    String saveFile(MultipartFile file);
    List<String> saveMultipleFiles(List<MultipartFile> files);


    boolean isVillageNameTaken(String villageName);
    boolean isVillageRegNumberTaken(String villageRegNumber);
    boolean isEmailTaken(String email);
    boolean isBankAccountNumberTaken(String bankAccountNumber);

    Village checkVillageExists(String villageEmail , String password);

    Village getVillageById(Long villageId);
    Village getVillageByEmail(String email);

    List<Village> getAllFilteredVillagesForAdmin(String villageName,String currentStatus,String villageStatus);
    List<Village> getVillageRequests();
    List<Village> searchAllVillagesByVillageNameAndVillageStatus(String villageName ,String villageStatus);
    List<Village> searchAllVillagesByVillageName(String villageName);
    List<Village> searchAllVillagesByCurrentStatusAndVillageStatus(String currentStatus ,String villageStatus);
    List<Village> searchAllVillagesByCurrentStatus(String currentStatus);
    List<Village> searchAllVillagesByVillageStatus(String villageStatus);
    List<Village> getAllVillages();


    Village updateVillageStatus(Long villageId , String status);
    boolean updateVillageStatusAndCascade(Long villageId, String newStatus);

    String updateVillageProfileImage(MultipartFile profileImg,Long villageId);
    boolean updateVillageImage(Long villageId,String oldImageName,MultipartFile newImageFile);
    String updateVillageDetails(VillageDto villageDto);

    Map<Long, Village> getCurrentVillageMapForVillageUpdate(List<TempVillage> tempVillages);

    Village approveVillageUpdateRequest(Long requestId, Map<String, String> updatePayload);
    TempVillage rejectVillageUpdateRequest(Long id, String message);

    String addMoreVillageImages(Long villageId , List<MultipartFile> files);
    String deleteVillageProfileImage(Long villageId);
    boolean deleteImageFromVillage(Long villageId, String imageName);

    List<String> findVillageLocationsContaining(String query);
    List<Village> findAvailableVillagesByLocationAndVillageName(String location, String villageName);

    String updateVillageEmail(@Valid VillageDto villageDto);

    String updatePassword(Long villageId , String newPassword);

}

