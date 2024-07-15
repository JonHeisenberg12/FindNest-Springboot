package com.lostandfound.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream serviceAccount = classLoader.getResourceAsStream("findnest-54a57-firebase-adminsdk-foypp-6ea79cebd6.json")) {

            if (serviceAccount == null) {
                throw new IllegalArgumentException("Firebase service account key file not found");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("findnest-54a57.appspot.com") // Replace with your Firebase Storage bucket name
                    .build();

            return FirebaseApp.initializeApp(options);
        }
    }
}
