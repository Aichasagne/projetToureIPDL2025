<<<<<<< HEAD
package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
=======
package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
>>>>>>> ff72f9ff3699386ffcb2638e42f7767a6addeee9
}