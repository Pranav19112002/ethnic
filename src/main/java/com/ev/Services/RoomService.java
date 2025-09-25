package com.ev.Services;

import com.ev.Model.StayRoomDetailDto;
import com.ev.Model.StayRoomDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RoomService {

    boolean isRoomNameTaken(String roomName ,Long stayId);

    StayRoomDetails saveRoom(StayRoomDetailDto roomDto);

    List<String> saveFile(List<MultipartFile> files);

    StayRoomDetails getRoomById(Long roomId);

    String updateRoom(StayRoomDetailDto roomDto);

    ResponseEntity<?> handleAddMoreImages(Long roomId, List<MultipartFile> files);

    ResponseEntity<?> handleUpdateImage(Long roomId, String oldImageName, MultipartFile newFile);

    ResponseEntity<?> handleDeleteImage(Long roomId, String imageName);

    StayRoomDetailDto getRoomDetailsById(Long roomId);
}
