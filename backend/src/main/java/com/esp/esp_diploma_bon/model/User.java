package com.esp.esp_diploma_bon.model;

import com.esp.esp_diploma_bon.model.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"users\"")  // Utilisez des guillemets pour échapper le mot réservé
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 20)
    private String matricule;

    @Column(length = 50)
    private String department;

    @Column(length = 50)
    private String program;

    @Column(name = "enrollment_year", length = 4)
    private String enrollmentYear;

    @Column(name = "graduation_year", length = 4)
    private String graduationYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}