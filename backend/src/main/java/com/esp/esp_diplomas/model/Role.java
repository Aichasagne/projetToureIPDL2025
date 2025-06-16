<<<<<<< HEAD
package com.esp.esp_diplomas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "student", "admin"
=======
package com.esp.esp_diplomas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "student", "admin"
>>>>>>> ff72f9ff3699386ffcb2638e42f7767a6addeee9
}