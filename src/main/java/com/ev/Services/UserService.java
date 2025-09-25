package com.ev.Services;

import com.ev.Model.User;
import com.ev.Model.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    boolean isEmailTakenByAnotherUser(String newEmail, Long currentUserId);

     boolean isPhoneTakenByAnotherUser(String newPhone, Long currentUserId);

    boolean isEmailTaken(String email);

    String saveUser(UserDto userDto);

    String saveFile(MultipartFile file);

    User checkUserExists(String userName , String userPassword);

    boolean findUserByEmail(String userEmail);

    List<User> findAllUsers();

    String deleteUserById(Long userId);

    User getUserById(Long userId);

    User getUserByEmail(String email);

    String updateUserImage(MultipartFile newImage , Long userId);

    String updateUserProfile(UserDto userDto);

    String resetPassword(UserDto userDto);

    String deleteUserImage(Long userId);

}

