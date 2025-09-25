package com.ev.Services;

import com.ev.Model.TempVillage;
import com.ev.Model.TempVillageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TempVillageService {

    String addUpdateVillageRequest(TempVillageDto tempVillageDto);

    String saveFile(MultipartFile file);

    List<TempVillage> getAllTempVillagesByStatus(String status);
    List<TempVillage> getVillageUpdateRequests(Long villageId);
    List<TempVillage> getAllVillageUpdates();
    List<TempVillage> getVillageUpdatesByStatus(String status);

    boolean isRequestExists(Long villageId,String status);

    boolean deleteUpdateVillageRequestById(Long requestId);

    TempVillage getTempVillageById(Long tempVillageId);
}
