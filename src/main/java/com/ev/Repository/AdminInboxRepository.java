package com.ev.Repository;

import com.ev.Model.AdminInboxMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminInboxRepository extends JpaRepository<AdminInboxMessage, Long> {

    List<AdminInboxMessage> findAllByOrderByTimestampDesc();
    List<AdminInboxMessage> findByIsReadFalse();

    @Transactional
    @Modifying
    @Query("UPDATE AdminInboxMessage m SET m.isRead = true WHERE m.id = :id")
    void markAsRead(@Param("id") Long id);

    // Mark all messages as read
    @Transactional
    @Modifying
    @Query("UPDATE AdminInboxMessage m SET m.isRead = true WHERE m.isRead = false")
    void markAllRead();

    // Delete all messages
    @Transactional
    @Modifying
    @Query("DELETE FROM AdminInboxMessage m")
    void deleteAllMessages();


}
