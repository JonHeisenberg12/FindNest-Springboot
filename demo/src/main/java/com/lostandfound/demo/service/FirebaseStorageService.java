package com.lostandfound.demo.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService {

    public String uploadFile(MultipartFile file) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();
        String blobName = file.getOriginalFilename();
        Blob blob = bucket.create(blobName, file.getInputStream(), file.getContentType());
        return blob.getMediaLink();
    }

    public String getFileUrl(String fileName) {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(fileName);
        if (blob == null) {
            return "File not found!";
        }
        URL signedUrl = blob.signUrl(1, TimeUnit.HOURS);
        return signedUrl.toString();
    }
}