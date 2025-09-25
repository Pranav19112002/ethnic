package com.ev.Repository;

import com.ev.Model.User;
import com.ev.Model.UserFeedBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFeedBackRepository extends JpaRepository<UserFeedBack,Long> {

    boolean existsByUser(User user);

    Optional<UserFeedBack> findByUser(User user);

    List<UserFeedBack> findTop15ByOrderBySubmittedAtDesc();


}
