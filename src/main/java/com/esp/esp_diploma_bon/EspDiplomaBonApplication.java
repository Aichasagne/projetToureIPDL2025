package com.esp.esp_diploma_bon;

import com.esp.esp_diploma_bon.model.Role;
import com.esp.esp_diploma_bon.model.User;
import com.esp.esp_diploma_bon.repository.RoleRepository;
import com.esp.esp_diploma_bon.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication
public class EspDiplomaBonApplication implements ApplicationListener<ContextRefreshedEvent> {
    public static void main(String[] args) {
        SpringApplication.run(EspDiplomaBonApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        UserRepository userRepository = event.getApplicationContext().getBean(UserRepository.class);
        RoleRepository roleRepository = event.getApplicationContext().getBean(RoleRepository.class);
        PasswordEncoder passwordEncoder = event.getApplicationContext().getBean(PasswordEncoder.class);

        if (userRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("admin");
            roleRepository.save(adminRole);

            Role studentRole = new Role();
            studentRole.setName("student");
            roleRepository.save(studentRole);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Administrateur");
            admin.setRole(adminRole);
            userRepository.save(admin);

            User student = new User();
            student.setUsername("student");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setName("Étudiant Test");
            student.setRole(studentRole);
        }

    }
}
