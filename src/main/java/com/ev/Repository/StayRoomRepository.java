package com.ev.Repository;

import com.ev.Model.StayRoomDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StayRoomRepository extends JpaRepository<StayRoomDetails, Long> {
}
