package com.ev.Services;

import com.ev.Model.*;
import com.ev.Repository.RoomRepository;
import com.ev.Repository.VillageStayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoomServices implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private VillageStayRepository villageStayRepository;

    private static final int MAX_IMAGES = 5;

    @Override
    public boolean isRoomNameTaken(String roomName, Long stayId) {
        return roomRepository.existsByRoomNameIgnoreCaseAndVillageStay_StayId(roomName,stayId);
    }

    @Override
    public StayRoomDetails saveRoom(StayRoomDetailDto roomDto) {
        if (roomDto.getStayId() == null) {
            throw new IllegalArgumentException("Stay ID cannot be null");
        }

        List<String> storageFileNames = saveFile(roomDto.getRoomPhotos());

        StayRoomDetails room = new StayRoomDetails();
        room.setRoomName(roomDto.getRoomName());
        room.setNumberOfRooms(roomDto.getNumberOfRooms());
        room.setAC(roomDto.isAC());
        room.setAvailableRooms(roomDto.getAvailableRooms());
        room.setPricePerRoom(roomDto.getPricePerRoom());
        room.setAmenities(roomDto.getAmenities());
        room.setNotes(roomDto.getNotes());
        room.setRoomType(roomDto.getRoomType());
        room.setRoomPhotoNames(storageFileNames);

        VillageStay villageStay = villageStayRepository.findById(roomDto.getStayId())
                .orElseThrow(() -> new RuntimeException("Stay not found"));
        room.setVillageStay(villageStay);

        return roomRepository.save(room);
    }


    @Override
    public List<String> saveFile(List<MultipartFile> files) {

        List<String> filePaths = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return filePaths;
        }
        try {
            String uploadDir = "src/main/resources/static/uploads/room/";
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
    public StayRoomDetails getRoomById(Long roomId) {
        Optional<StayRoomDetails> room = roomRepository.findById(roomId);
        return room.orElse(null);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @Override
    public String updateRoom(StayRoomDetailDto roomDto) {
        Optional<StayRoomDetails> optionalStayRoomDetails = roomRepository.findById(roomDto.getId());
        if (optionalStayRoomDetails.isPresent()){
            StayRoomDetails room = optionalStayRoomDetails.get();

            room.setRoomName(roomDto.getRoomName());
            room.setAC(roomDto.isAC());
            room.setAmenities(roomDto.getAmenities());
            room.setRoomType(roomDto.getRoomType());
            room.setNotes(roomDto.getNotes());
            room.setPricePerRoom(roomDto.getPricePerRoom());
            room.setNumberOfRooms(roomDto.getNumberOfRooms());
            room.setAvailableRooms(roomDto.getAvailableRooms());
            roomRepository.save(room);
            return "success";
        }
        return "Room not found";
    }

    @Override
    public ResponseEntity<?> handleAddMoreImages(Long roomId, List<MultipartFile> files) {
        StayRoomDetails room = getRoomById(roomId);
        int existingCount = room.getRoomPhotoNames().size();
        int availableSlots = MAX_IMAGES - existingCount;

        if (files.size() > availableSlots) {
            return ResponseEntity.badRequest()
                    .body("Only " + availableSlots + " image(s) can be added.");
        }

        List<String> savedFiles = saveFile(files);
        room.getRoomPhotoNames().addAll(savedFiles);
        roomRepository.save(room);

        return ResponseEntity.ok("Images added successfully");
    }

    @Override
    public ResponseEntity<?> handleUpdateImage(Long roomId, String oldImageName, MultipartFile newFile) {
        StayRoomDetails room = getRoomById(roomId);
        List<String> photos = room.getRoomPhotoNames();
        int index = photos.indexOf(oldImageName);

        if (index == -1) {
            return ResponseEntity.badRequest().body("Old image not found");
        }

        deleteFile(oldImageName);
        String newFileName = saveFile(List.of(newFile)).get(0);
        photos.set(index, newFileName);
        roomRepository.save(room);

        return ResponseEntity.ok("Image updated successfully");
    }

    @Override
    public ResponseEntity<?> handleDeleteImage(Long roomId, String imageName) {
        StayRoomDetails room = getRoomById(roomId);
        boolean removed = room.getRoomPhotoNames().remove(imageName);

        if (!removed) {
            return ResponseEntity.badRequest().body("Image not found in room");
        }

        deleteFile(imageName);
        roomRepository.save(room);

        return ResponseEntity.ok("Image deleted successfully");
    }

    private void deleteFile(String filename) {
        try {
            Path path = Paths.get("src/main/resources/static/uploads/room/").resolve(filename);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + filename);
        }
    }

    @Override
    public StayRoomDetailDto getRoomDetailsById(Long roomId) {
        StayRoomDetails room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

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
    }
}
