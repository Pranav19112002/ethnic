package com.ev.Services;

import com.ev.Model.TempVillage;
import com.ev.Model.TempVillageDto;
import com.ev.Repository.TempVillageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TempVillageServices implements TempVillageService{

    @Autowired
    private TempVillageRepository tempVillageRepository;

    @Override
    public String addUpdateVillageRequest(TempVillageDto tempVillageDto) {
        if (tempVillageDto == null){
            return "Data is Empty";
        }
        String OffDocFileName = saveFile(tempVillageDto.getOfficialDoc());
        String stampOrSealName =  saveFile(tempVillageDto.getOffStampOrSeal());

        TempVillage tempVillage = new TempVillage();
        tempVillage.setVillageId(tempVillageDto.getVillageId());
        tempVillage.setVillageName(tempVillageDto.getVillageName());
        tempVillage.setVillageEmail(tempVillageDto.getVillageEmail());
        tempVillage.setVillageLocation(tempVillageDto.getVillageLocation());
        tempVillage.setVillageRegNumber(tempVillageDto.getVillageRegNumber());
        tempVillage.setVillageAuthorityName(tempVillageDto.getVillageAuthorityName());
        tempVillage.setVillageDescription(tempVillageDto.getVillageDescription());
        tempVillage.setLegalRepresentativeName(tempVillageDto.getLegalRepresentativeName());
        tempVillage.setRepresentativePosition(tempVillageDto.getRepresentativePosition());
        tempVillage.setOfficialIdNumber(tempVillageDto.getOfficialIdNumber());
        tempVillage.setContactPersonName(tempVillageDto.getContactPersonName());
        tempVillage.setContactEmail(tempVillageDto.getContactEmail());
        tempVillage.setContactNo(tempVillageDto.getContactNo());
        tempVillage.setAltContactNo(tempVillageDto.getAltContactNo());
        tempVillage.setBankName(tempVillageDto.getBankName());
        tempVillage.setBankAccountNumber(tempVillageDto.getBankAccountNumber());
        tempVillage.setTin(tempVillageDto.getTaxInfoNumber());
        tempVillage.setVillageType(tempVillageDto.getVillageType());
        tempVillage.setLatitude(tempVillageDto.getLatitude());
        tempVillage.setLongitude(tempVillageDto.getLongitude());
        tempVillage.setOffDocFileName(OffDocFileName);
        tempVillage.setOffStampOrSealFileName(stampOrSealName);
        tempVillage.setRequestedAt(LocalDate.now());
        tempVillage.setStatus("Pending");
        tempVillageRepository.save(tempVillage);
        return "success";
    }

    @Override
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()){
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
        }catch (Exception ex) {
            System.out.println("File upload error: " + ex.getMessage());
        }
        return fileName;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }


    @Override
    public List<TempVillage> getAllTempVillagesByStatus(String status) {
        return tempVillageRepository.findByStatusIgnoreCase(status);
    }

    @Override
    public List<TempVillage> getVillageUpdateRequests(Long villageId) {
        return tempVillageRepository.findByVillageIdOrderByIdDesc(villageId);
    }

    @Override
    public List<TempVillage> getAllVillageUpdates() {
        return tempVillageRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<TempVillage> getVillageUpdatesByStatus(String status) {
        return tempVillageRepository.findByStatusIgnoreCase(status);
    }


    @Override
    public boolean isRequestExists(Long villageId, String status) {
        return tempVillageRepository.existsByVillageIdAndStatusIgnoreCase(villageId,status);
    }

    @Override
    public boolean deleteUpdateVillageRequestById(Long requestId) {
        Optional<TempVillage> optionalTempVillage = tempVillageRepository.findById(requestId);
        if (optionalTempVillage.isPresent()){
            tempVillageRepository.deleteById(requestId);
            return true;
        }
        return false;
    }

    @Override
    public TempVillage getTempVillageById(Long tempVillageId) {
        return tempVillageRepository.findByVillageIdAndStatusIgnoreCase(tempVillageId,"Pending");
    }
}
