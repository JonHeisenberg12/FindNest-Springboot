package com.lostandfound.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank
    private String firstname;

    private String middlename;

    @NotBlank
    private String lastname;

    @NotBlank
    @Size(min = 7, max = 20)
    @Indexed(unique = true)
    private String username;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String department;

    private String profilePicture = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Windows_10_Default_Profile_Picture.svg/1200px-Windows_10_Default_Profile_Picture.svg.png";

    private Role role = Role.staff;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

    // Enum for role with case-sensitive values
    public enum Role {
        superAdmin, admin, staff;

        public static Role fromValue(String value) {
            for (Role role : Role.values()) {
                if (role.name().equalsIgnoreCase(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("No enum constant " + Role.class.getCanonicalName() + "." + value);
        }
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
