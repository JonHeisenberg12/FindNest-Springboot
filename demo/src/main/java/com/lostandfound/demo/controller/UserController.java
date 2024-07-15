package com.lostandfound.demo.controller;


import com.lostandfound.demo.model.User;
import com.lostandfound.demo.repository.UserRepository;
import com.lostandfound.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("API IS WORKING! :>!");
    }

    private boolean checkSuperAdminExists() {
        return userService.findAllUsers().stream().anyMatch(user -> "superAdmin".equals(user.getRole()));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody User user, @RequestParam("role") String role, @RequestParam String requestUserId) {
        Optional<User> existingUserOpt = userService.findUserById(userId);
        if (!existingUserOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User existingUser = existingUserOpt.get();

        if (!"admin".equals(requestUserId) && !"superAdmin".equals(requestUserId) && !existingUser.getId().equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this user");
        }

        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (user.getPassword().length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 6 characters long");
            }
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existingUser.getPassword());
        }

        if (user.getUsername() != null) {
            if (user.getUsername().length() < 7 || user.getUsername().length() > 20 || user.getUsername().contains(" ") || !user.getUsername().matches("^[a-zA-Z0-9]+$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username");
            }
        }

        if (role != null && !role.trim().isEmpty()) {
            if (!"superAdmin".equals(requestUserId) && !"admin".equals(requestUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins and super admins can change user roles");
            }
            if ("superAdmin".equals(role) && checkSuperAdminExists()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("There can only be one superAdmin");
            }
            user.setRole(User.Role.fromValue(role));
        }

        user.setId(userId);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable String userId, @RequestBody ChangePasswordRequest request) {
        Optional<User> existingUserOpt = userService.findUserById(userId);
        if (!existingUserOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = existingUserOpt.get();
        if (!bCryptPasswordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password is incorrect");
        }

        user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        userService.saveUser(user);
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId, @RequestParam String requestUserId) {
        Optional<User> existingUserOpt = userService.findUserById(userId);
        if (!existingUserOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = existingUserOpt.get();

        if ("admin".equals(requestUserId)) {
            if ("admin".equals(user.getRole()) || "superAdmin".equals(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admins cannot delete other admins or super admins");
            }
        } else if (!"superAdmin".equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only super admins and admins can delete users.");
        }

        userService.deleteUserById(userId);
        return ResponseEntity.ok("User has been deleted");
    }

    @GetMapping("/signout")
    public ResponseEntity<?> signout() {
        // Add logic to handle sign out, if necessary
        return ResponseEntity.ok("User has been signed out");
    }

    @GetMapping
    public ResponseEntity<?> getUsers(@RequestParam(name = "excludeUserId", required = false) String excludeUserId,
                                      @RequestParam(name = "startIndex", defaultValue = "0") int startIndex,
                                      @RequestParam(name = "limit", defaultValue = "10") int limit,
                                      @RequestParam(name = "sort", defaultValue = "desc") String sortDirection,
                                      @RequestParam String requestUserId) {
        if (!"admin".equals(requestUserId) && !"superAdmin".equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to view users");
        }

        List<User> users = userService.findAllUsers();
        users.removeIf(user -> excludeUserId != null && excludeUserId.equals(user.getId()));

        if ("admin".equals(requestUserId)) {
            users.removeIf(user -> !"staff".equals(user.getRole()));
        }

        users.sort((u1, u2) -> {
            if ("asc".equals(sortDirection)) {
                return u1.getCreatedAt().compareTo(u2.getCreatedAt());
            } else {
                return u2.getCreatedAt().compareTo(u1.getCreatedAt());
            }
        });

        int endIndex = Math.min(startIndex + limit, users.size());
        List<User> paginatedUsers = users.subList(startIndex, endIndex);

        long totalUsers = userService.countUsersByCreatedAtAfter(new Date(new Date().getTime() - (30L * 24 * 60 * 60 * 1000)));

        return ResponseEntity.ok(new UsersResponse(paginatedUsers, users.size(), totalUsers));
    }
}

class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;

    // Getters and setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

class UsersResponse {
    private List<User> users;
    private long totalUsers;
    private long lastMonthUsers;

    public UsersResponse(List<User> users, long totalUsers, long lastMonthUsers) {
        this.users = users;
        this.totalUsers = totalUsers;
        this.lastMonthUsers = lastMonthUsers;
    }

    // Getters and setters
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getLastMonthUsers() {
        return lastMonthUsers;
    }

    public void setLastMonthUsers(long lastMonthUsers) {
        this.lastMonthUsers = lastMonthUsers;
    }
}
