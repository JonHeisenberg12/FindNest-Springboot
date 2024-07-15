package com.lostandfound.demo;

import com.lostandfound.demo.model.User;
import com.lostandfound.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail() {
        Optional<User> userOptional = userRepository.findByEmail("superAdminuser@test.com");
        assertThat(userOptional).isPresent();
        User user = userOptional.get();
        assertThat(user.getEmail()).isEqualTo("superAdminuser@test.com");
    }

    @Test
    public void testFindByUsername() {
        Optional<User> userOptional = userRepository.findByUsername("superadminuser");
        assertThat(userOptional).isPresent();
        User user = userOptional.get();
        assertThat(user.getUsername()).isEqualTo("superadminuser");
    }

    @Test
    public void testCountByCreatedAtGreaterThanEqual() {
        long count = userRepository.countByCreatedAtGreaterThanEqual(new Date(System.currentTimeMillis() - 86400000L)); // 1 day ago
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
