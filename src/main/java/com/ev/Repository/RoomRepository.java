package com.ev.Repository;

import com.ev.Model.StayRoomDetails;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<StayRoomDetails , Long> {

    boolean existsByRoomNameIgnoreCaseAndVillageStay_StayId(String roomName,Long stayId);
}
