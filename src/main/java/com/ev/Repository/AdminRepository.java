package com.ev.Repository;

import com.ev.Model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin , String> {

    Optional<Admin> findByAdminIdIgnoreCase(String adminId);
    Optional<Admin> findByAdminEmailIgnoreCase(String adminEmail);
    Optional<Admin> findByAdminContactNo(String contactNo);
}

