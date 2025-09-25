package com.ev.Repository;

import com.ev.Model.Village;
import com.ev.Model.VillageInboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageInboxRepository extends JpaRepository<VillageInboxMessage, Long> {

    List<VillageInboxMessage> findByVillageOrderByIsReadAscTimestampDesc(Village village);

    List<VillageInboxMessage> findByVillageAndIsReadFalseOrderByTimestampDesc(Village village);

    @Modifying
    @Query("UPDATE VillageInboxMessage m SET m.isRead = true WHERE m.id = :id")
    void markMessageAsRead(@Param("id") Long id);

    @Modifying
    @Query("UPDATE VillageInboxMessage m SET m.isRead = true WHERE m.village = :village AND m.isRead = false")
    void markAllAsRead(@Param("village") Village village);

}
