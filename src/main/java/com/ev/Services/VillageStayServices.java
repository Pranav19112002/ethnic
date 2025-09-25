package com.ev.Services;

import com.ev.Model.*;
import com.ev.Repository.VillageRepository;
import com.ev.Repository.VillageStayRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VillageStayServices implements VillageStayService{

    @Autowired
    private VillageStayRepository villageStayRepository;

    @Autowired
    private VillageRepository villageRepository;

    private final String uploadDir = "src/main/resources/static/uploads/stay/";

    @Override
    public boolean isStayNameTaken(String stayName, Long villageId) {
        return villageStayRepository.existsByVillageStayNameIgnoreCaseAndVillage_VillageId(stayName,villageId);
    }

    @Override
    public VillageStay saveStay(VillageStayDto villageStayDto) {
        if (villageStayDto.getVillageId() == null) {
            throw new IllegalArgumentException("Village ID cannot be null");
        }

        List<String> storageFileNames = saveFile(villageStayDto.getStayPhotos());

        VillageStay stay = new VillageStay();
        stay.setVillageStayName(villageStayDto.getVillageStayName());
        stay.setStayType(villageStayDto.getStayType());
        stay.setStayPlace(villageStayDto.getStayPlace());
        stay.setDescription(villageStayDto.getDescription());
        stay.setContactPersonName(villageStayDto.getContactPersonName());
        stay.setContactNo(villageStayDto.getContactNo());
        stay.setAltContactNo(villageStayDto.getAltContactNo());
        stay.setContactEmail(villageStayDto.getContactEmail());
        stay.setStayPhotosNames(storageFileNames);
        stay.setPriceStartsFrom(villageStayDto.getPriceStartsFrom());
        stay.setActive(villageStayDto.isActive());
        stay.setCreatedAt(LocalDateTime.now());

        Village village = villageRepository.findById(villageStayDto.getVillageId())
                .orElseThrow(() -> new RuntimeException("Village not found"));
        stay.setVillage(village);

        return villageStayRepository.save(stay);
    }


    @Override
    public List<String> saveFile(List<MultipartFile> files) {

        List<String> filePaths = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return filePaths;
        }
        try {
            String uploadDir = "src/main/resources/static/uploads/stay/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            for (MultipartFile file : files){
                if (!file.isEmpty()){
                    String originalFilename = file.getOriginalFilename();
                    String sanitizedFilename = sanitizeFilename(originalFilename);
                    String fileName =System.currentTimeMillis() +"_"+sanitizedFilename;
                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);
                    filePaths.add(fileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving files: " + e.getMessage());
        }
        return filePaths;
    }

    @Override
    public List<VillageStay> filterStaysByVillageId(Long filterStayId, String filterStayName, Boolean filterStatus, Long villageId) {
        List<VillageStay> villageStays = villageStayRepository.findByVillage_VillageIdOrderByStayIdDesc(villageId);

        return villageStays.stream()
                .filter(ab -> filterStayId == null || ab.getStayId().equals(filterStayId))
                .filter(ab -> filterStayName == null || filterStayName.isEmpty() ||
                        ab.getVillageStayName().toLowerCase().startsWith(filterStayName.toLowerCase()))
                .filter(ab -> filterStatus == null || ab.isActive() == filterStatus)
                .collect(Collectors.toList());
    }



    private String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @Override
    public VillageStayDto getStayDetailsById(Long stayId) {
        VillageStay stay = villageStayRepository.findById(stayId)
                .orElseThrow(() -> new RuntimeException("Stay not found"));

        VillageStayDto dto = new VillageStayDto();
        dto.setStayId(stay.getStayId());
        dto.setVillageStayName(stay.getVillageStayName());
        dto.setStayType(stay.getStayType());
        dto.setStayPlace(stay.getStayPlace());
        dto.setContactNo(stay.getContactNo());
        dto.setContactEmail(stay.getContactEmail());
        dto.setActive(stay.isActive());
        dto.setDescription(stay.getDescription());
        dto.setPriceStartsFrom(stay.getPriceStartsFrom());
        dto.setStayPhotosNames(stay.getStayPhotosNames());

        List<StayRoomDetailDto> roomDto = stay.getRoomDetails().stream().map(room -> {
            StayRoomDetailDto r = new StayRoomDetailDto();
            r.setId(room.getRoomId()); // ✅ Corrected
            r.setRoomName(room.getRoomName());
            r.setRoomType(room.getRoomType());
            r.setAC(room.isAC());
            r.setAvailableRooms(room.getAvailableRooms());
            r.setPricePerRoom(room.getPricePerRoom());
            r.setRoomPhotoNames(room.getRoomPhotoNames());
            r.setAmenities(room.getAmenities());
            r.setNotes(room.getNotes());
            return r;
        }).toList();

        dto.setRooms(roomDto);
        return dto;
    }

    @Override
    public void updateStatus(Long stayId, boolean isActive) {
        VillageStay stay = villageStayRepository.findById(stayId)
                .orElseThrow(() -> new EntityNotFoundException("Stay not found"));
        stay.setActive(isActive);
        villageStayRepository.save(stay);
    }

    @Override
    public VillageStay getVillageStayById(Long stayId) {
       Optional<VillageStay> vs = villageStayRepository.findById(stayId);
        return vs.orElse(null);
    }

    @Override
    public void updateStayImage(Long stayId, String oldImageName, MultipartFile newImage) {
        VillageStay stay = villageStayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found"));

        // Delete old image
        try {
            Files.deleteIfExists(Paths.get(uploadDir + oldImageName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete old image: " + e.getMessage());
        }

        // Save new image
        List<String> newFileNames = saveFile(Collections.singletonList(newImage));
        if (!newFileNames.isEmpty()) {
            List<String> photos = stay.getStayPhotosNames();
            int index = photos.indexOf(oldImageName);
            if (index != -1) {
                photos.set(index, newFileNames.get(0));
                stay.setStayPhotosNames(photos);
                villageStayRepository.save(stay);
            }
        }
    }

    @Override
    public void deleteStayImage(Long stayId, String imageName) {
        VillageStay stay = villageStayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found"));

        try {
            Files.deleteIfExists(Paths.get(uploadDir + imageName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }

        List<String> photos = stay.getStayPhotosNames();
        photos.remove(imageName);
        stay.setStayPhotosNames(photos);
        villageStayRepository.save(stay);
    }

    @Override
    public void addStayImages(Long stayId, List<MultipartFile> files) {
        VillageStay stay = villageStayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found"));

        List<String> newFileNames = saveFile(files);
        List<String> photos = stay.getStayPhotosNames();
        photos.addAll(newFileNames);
        stay.setStayPhotosNames(photos);
        villageStayRepository.save(stay);
    }

    @Override
    public String updateStay(VillageStayDto stayDto) {
        Optional<VillageStay> optionalVillageStay = villageStayRepository.findById(stayDto.getStayId());
        if (optionalVillageStay.isPresent()){

            VillageStay stay = optionalVillageStay.get();

            stay.setVillageStayName(stayDto.getVillageStayName());
            stay.setStayType(stayDto.getStayType());
            stay.setStayPlace(stayDto.getStayPlace());
            stay.setContactEmail(stayDto.getContactEmail());
            stay.setAltContactNo(stayDto.getAltContactNo());
            stay.setContactPersonName(stayDto.getContactPersonName());
            stay.setContactNo(stayDto.getContactNo());
            stay.setDescription(stayDto.getDescription());
            stay.setPriceStartsFrom(stayDto.getPriceStartsFrom());
            villageStayRepository.save(stay);
            return "success";
        }
        return "Stay not found";
    }

    @Override
    public List<VillageStay> filterStays(String villageName, String region) {
        return villageStayRepository.filterStays(villageName, region);
    }
}

