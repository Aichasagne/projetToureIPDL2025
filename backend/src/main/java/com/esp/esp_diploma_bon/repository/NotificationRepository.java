package com.esp.esp_diploma_bon.repository;


import com.esp.esp_diploma_bon.model.Notification;
import com.esp.esp_diploma_bon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
}