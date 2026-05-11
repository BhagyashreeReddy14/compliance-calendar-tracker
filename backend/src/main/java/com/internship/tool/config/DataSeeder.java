package com.internship.tool.config;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.entity.User;
import com.internship.tool.repository.ComplianceRepository;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ComplianceRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .build());
            
            userRepository.save(User.builder()
                    .email("manager@example.com")
                    .password(passwordEncoder.encode("manager123"))
                    .role(User.Role.MANAGER)
                    .build());
        }

        if (repository.count() == 0) {
            String[] categories = {"TAX", "LEGAL", "HR", "IT", "FINANCE"};
            String[] statuses = {"PENDING", "IN_PROGRESS", "COMPLETED", "OVERDUE"};
            String[] priorities = {"LOW", "MEDIUM", "HIGH"};

            for (int i = 1; i <= 30; i++) {
                repository.save(ComplianceRecord.builder()
                        .title("Compliance Task " + i)
                        .description("Detailed description for task " + i)
                        .category(categories[i % categories.length])
                        .status(statuses[i % statuses.length])
                        .priority(priorities[i % priorities.length])
                        .dueDate(LocalDateTime.now().plusDays(i))
                        .assignedTo("user" + i + "@example.com")
                        .deleted(false)
                        .build());
            }
        }
    }
}
