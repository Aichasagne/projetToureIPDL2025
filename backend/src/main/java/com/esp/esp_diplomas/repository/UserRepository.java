<<<<<<< HEAD
package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
=======
package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
>>>>>>> ff72f9ff3699386ffcb2638e42f7767a6addeee9
}