package com.lostandfound.demo.repository;

import com.lostandfound.demo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    long countByCreatedAtGreaterThanEqual(Date date);
    List<User> findAll();
}
