package com.lostandfound.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.lostandfound.demo.model.Item;

import java.util.List;


public interface ItemRepository extends MongoRepository<Item, String> {
    long countByStatus(String status);
}
