package com.lostandfound.demo.controller;

import com.lostandfound.demo.service.FirebaseStorageService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    private FirebaseStorageService storageService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null) {
            return "No file received";
        }
        try {
            return storageService.uploadFile(file);
        } catch (IOException e) {
            return "Error uploading file: " + e.getMessage();
        }
    }

    @GetMapping("/get-url")
    public String getFileUrl(@RequestParam("fileName") String fileName) {
        return storageService.getFileUrl(fileName);
    }
}
