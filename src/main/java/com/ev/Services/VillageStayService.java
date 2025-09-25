package com.ev.Services;

import com.ev.Model.ActivityBooking;
import com.ev.Model.VillageStay;
import com.ev.Model.VillageStayDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VillageStayService {
    boolean isStayNameTaken(String stayName, Long villageId);

    VillageStay saveStay(VillageStayDto villageStayDto);

    List<String> saveFile(List<MultipartFile> files);

    List<VillageStay> filterStaysByVillageId(Long filterStayId , String filterStayName, Boolean filterStatus , Long villageId);

    VillageStayDto getStayDetailsById(Long stayId);

    void updateStatus(Long stayId, boolean isActive);

    VillageStay getVillageStayById(Long stayId);

    void updateStayImage(Long stayId, String oldImageName, MultipartFile newImage);

    void deleteStayImage(Long stayId, String imageName);

    void addStayImages(Long stayId, List<MultipartFile> files);

    String updateStay(VillageStayDto stayDto);

    List<VillageStay> filterStays(String villageName, String region);
}
