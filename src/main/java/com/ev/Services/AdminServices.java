package com.ev.Services;

import com.ev.Model.Admin;
import com.ev.Model.AdminDto;
import com.ev.Repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class AdminServices implements AdminService{

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public Admin checkAdminExits(String adminId, String adminPassword) {
        Optional<Admin> optionalAdmin = adminRepository.findByAdminIdIgnoreCase(adminId);
        if (optionalAdmin.isPresent())
        {
            Admin admin = optionalAdmin.get();
            if (adminPassword.equals(admin.getAdminPassword())){
                return admin;
            }
        }
        return null;
    }

    @Override
    public Admin findAdminById(String adminId) {
        Optional<Admin> adminOptional = adminRepository.findByAdminIdIgnoreCase(adminId);
        return adminOptional.orElse(null);
    }

    @Override
    public String updateAdmin(AdminDto adminDto, String adminId) {
        if (isEmailTakenByAnotherAdmin(adminDto.getAdminEmail(), adminId)) {
            return "Email already taken by Another Admin.";
        }
        if (isContactTakenByAnotherAdmin(adminDto.getAdminContactNo(), adminId)) {
            return "Contact No is already taken by Another Admin.";
        }

        Optional<Admin> optionalAdmin = adminRepository.findByAdminIdIgnoreCase(adminId);
        if (optionalAdmin.isPresent()) {
            Admin admin = optionalAdmin.get();
            MultipartFile imageFile = adminDto.getAdminProfileImageFile();

            // 🔄 Handle image replacement
            if (imageFile != null && !imageFile.isEmpty()) {
                String oldImage = admin.getAdminProfileImage();
                if (oldImage != null && !oldImage.isBlank()) {
                    deleteFile(oldImage); // ⛔ Delete old image from storage
                }

                String newFileName = saveFile(imageFile); // 📥 Save new image
                if (newFileName != null) {
                    admin.setAdminProfileImage(newFileName);
                }
            }

            // 📝 Update other fields
            admin.setAdminName(adminDto.getAdminName());
            admin.setAdminEmail(adminDto.getAdminEmail());
            admin.setAdminContactNo(adminDto.getAdminContactNo());

            adminRepository.save(admin);
            return "success";
        }

        return "failed";
    }

    public void deleteFile(String fileName) {
        Path filePath = Paths.get("src/main/resources/static/uploads/admin/" + fileName);
        try {
            Files.deleteIfExists(filePath);
            System.out.println("🗑️ Deleted old image: " + fileName);
        } catch (IOException e) {
            System.out.println("⚠️ Could not delete image: " + fileName);
            e.printStackTrace();
        }
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
            String uploadDir = "src/main/resources/static/uploads/admin/";
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
    public boolean isEmailTakenByAnotherAdmin(String adminEmail, String currentAdminId) {
        Optional<Admin> adminWithEmail = adminRepository.findByAdminEmailIgnoreCase(adminEmail);
        return adminWithEmail.isPresent() && !adminWithEmail.get().getAdminId().equalsIgnoreCase(currentAdminId);
    }

    @Override
    public boolean isContactTakenByAnotherAdmin(String contactNo, String currentAdminId) {
        Optional<Admin> adminWithContact = adminRepository.findByAdminContactNo(contactNo);
        return adminWithContact.isPresent() && !adminWithContact.get().getAdminId().equalsIgnoreCase(currentAdminId);
    }

    @Override
    public Admin updateAdminPassword(String adminId,String newPassword) {
        System.out.println("Admin Id:"+adminId);
        System.out.println("Admin Pass:"+newPassword);
        Optional<Admin> optionalAdmin = adminRepository.findByAdminIdIgnoreCase(adminId);
        if (optionalAdmin.isPresent()){
            Admin admin = optionalAdmin.get();
            admin.setAdminPassword(newPassword);
            adminRepository.save(admin);
            return admin;
        }
        return null;
    }
}

