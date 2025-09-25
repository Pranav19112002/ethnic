package com.ev.Services;

import com.ev.Model.User;
import com.ev.Model.UserDto;
import com.ev.Repository.UserRepository;
import com.ev.Repository.VillageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class UserServices implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VillageRepository villageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public boolean isEmailTakenByAnotherUser(String newEmail, Long currentUserId) {
        Optional<User> userWithEmail = userRepository.findByUserEmail(newEmail);
        return userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(currentUserId);
    }

    @Override
    public boolean isPhoneTakenByAnotherUser(String newPhone, Long currentUserId) {
        Optional<User> userWithPhone = userRepository.findByUserPhone(newPhone);
        return userWithPhone.isPresent() && !userWithPhone.get().getUserId().equals(currentUserId);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.findByUserEmail(email).isPresent()
                || villageRepository.findByVillageEmail(email).isPresent();
    }

    @Override
    public String saveUser(UserDto userDto) {

        User user = new User();
        user.setUserEmail(userDto.getUserEmail());
        user.setUserName(userDto.getUserName());
        user.setUserPhone(userDto.getUserPhone());
        user.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));
        userRepository.save(user);
        return "success";
    }

    @Override
    public User checkUserExists(String userEmail, String userPassword) {
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail);
        if(optionalUser.isPresent())
        {
            User user = optionalUser.get();
            if (passwordEncoder.matches(userPassword,user.getUserPassword()))
            {
                return user;
            }
        }
        return null;
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
            String uploadDir = "src/main/resources/static/uploads/user/";
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
    public boolean findUserByEmail(String userEmail) {
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail);
        return optionalUser.isPresent();
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByUserIdDesc();
    }

    @Override
    public String deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            return "Failed Delete";
        }
        userRepository.deleteById(userId);
        return "success";
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByUserEmail(email);
        return optionalUser.orElse(null);
    }

    @Override
    public String updateUserImage(MultipartFile newImage , Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()){
            return "User Not Found";
        }

        User user = optionalUser.get();

        String oldFileName = user.getUserProfilePicName();
        File oldFile = new File("src/main/resources/static/uploads/user/" + oldFileName);
        if (oldFile.exists()){
            oldFile.delete();
        }

        String newImageName = saveFile(newImage);
        if (newImageName.isEmpty()){
            return "Error";
        }

        user.setUserProfilePicName(newImageName);
        userRepository.save(user);
        return "success";
    }

    @Override
    public String updateUserProfile(UserDto userDto) {
        Optional<User> optionalUser = userRepository.findById(userDto.getUserId());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();

            if (isEmailTakenByAnotherUser(userDto.getUserEmail(),userDto.getUserId())){
                return "email-exists";
            }
            if (isPhoneTakenByAnotherUser(userDto.getUserPhone(),userDto.getUserId())){
                return "phone-exists";
            }
            user.setUserEmail(userDto.getUserEmail());
            user.setUserName(userDto.getUserName());
            user.setUserPhone(userDto.getUserPhone());
            userRepository.save(user);
            return "success";
        }
        return "no-user";
    }

    @Override
    public String resetPassword(UserDto userDto) {
        Optional<User> optionalUser = userRepository.findById(userDto.getUserId());
        if (optionalUser.isEmpty()){
            return "no-user";
        }
        User user = optionalUser.get();

        if (userDto.getUserPassword().equals(user.getUserPassword()))
        {
            if (userDto.getNewPassword().equals(userDto.getConfirmPassword())){
                user.setUserPassword(userDto.getNewPassword());
                userRepository.save(user);
                return "success";
            }
            else {
                return "password-miss-match";
            }
        }else {
            return "password-error";
        }
    }

    @Override
    public String deleteUserImage(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            if (user.getUserProfilePicName() != null){
                File file = new File("src/main/resources/static/uploads/user/" + user.getUserProfilePicName());
                if (file.exists()){
                    file.delete();
                    user.setUserProfilePicName(null);
                    userRepository.save(user);
                    return "success";
                }
                else {
                    return "Failed";
                }
            }
            else {
                return "Failed";
            }
        }
        else {
            return "Failed";
        }
    }
}

