package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.Notification;
import com.esp.esp_diplomas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
}