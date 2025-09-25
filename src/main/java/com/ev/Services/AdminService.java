package com.ev.Services;

import com.ev.Model.Admin;
import com.ev.Model.AdminDto;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService {

    Admin checkAdminExits(String adminId , String adminPassword);
    Admin findAdminById(String adminId);

    String updateAdmin(AdminDto adminDto,String adminId);

    String saveFile(MultipartFile file);

    boolean isEmailTakenByAnotherAdmin(String adminEmail,String currentAdminId);
    boolean isContactTakenByAnotherAdmin(String contactNo,String currentAdminId);

    Admin updateAdminPassword(String adminId,String newPassword);
}
