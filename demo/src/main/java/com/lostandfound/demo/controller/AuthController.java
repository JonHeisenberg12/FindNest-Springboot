package com.lostandfound.demo.controller;

import com.lostandfound.demo.model.User;
import com.lostandfound.demo.repository.UserRepository;
import com.lostandfound.demo.util.JwtTokenUtil;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
            );
        } catch (Exception e) {
            logger.error("Authentication failed for email: " + authenticationRequest.getEmail(), e);
            throw new Exception("Incorrect email or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @PostMapping("/createuser")
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return ResponseEntity.badRequest().body("Invalid token format");
        }
        
        logger.info("Token received: " + token);
        
        String department = user.getDepartment();
        String[] allowedDepartments = {"SSG", "SSO", "SSD"};
        boolean isValidDepartment = false;

        for (String dept : allowedDepartments) {
            if (dept.equals(department)) {
                isValidDepartment = true;
                break;
            }
        }

        if (!isValidDepartment) {
            return ResponseEntity.badRequest().body("Invalid department. Department must be one of: SSG, SSO, SSD");
        }

        if (user.getFirstname() == null || user.getLastname() == null || user.getUsername() == null ||
            user.getEmail() == null || user.getPassword() == null || user.getDepartment() == null) {
            return ResponseEntity.badRequest().body("All fields are required except middlename");
        }

        user.setUsername(user.getUsername().toLowerCase());
        user.setEmail(user.getEmail().toLowerCase());

        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Username or email is already in use");
        }

        existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Username or email is already in use");
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        String role = jwtTokenUtil.getRoleFromToken(token);
        if ("superAdmin".equals(role) && user.getRole() != null) {
            user.setRole(user.getRole());
        }

        userRepository.save(user);
        return ResponseEntity.ok("User created successfully");
    }
}

class AuthenticationRequest {
    private String email;
    private String password;

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class AuthenticationResponse {
    private final String jwt;

    public AuthenticationResponse(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }
}
