package com.esp.esp_diploma_bon.repository;


import com.esp.esp_diploma_bon.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}