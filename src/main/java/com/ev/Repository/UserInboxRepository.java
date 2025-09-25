package com.ev.Repository;

import com.ev.Model.UserInboxMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInboxRepository extends JpaRepository<UserInboxMessage, Long> {
    List<UserInboxMessage> findByUserIdOrderByTimestampDesc(Long userId);

    List<UserInboxMessage> findByUserIdOrderByIsReadAscTimestampDesc(Long userId);

    List<UserInboxMessage> findByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserInboxMessage m SET m.isRead = true WHERE m.id = :id")
    void markMessageAsRead(@Param("id") Long id);


    @Modifying
    @Transactional
    @Query("UPDATE UserInboxMessage m SET m.isRead = true WHERE m.userId = :userId AND m.isRead = false")
    void markAllMessagesAsRead(@Param("userId") Long userId);


}

