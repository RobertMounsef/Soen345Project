package com.ticket.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Value("${firebase.service.account}")
    private String serviceAccountFile;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream(serviceAccountFile);

        if (serviceAccount == null) {
            throw new IllegalStateException(
                "Firebase service account key not found at classpath:" + serviceAccountFile +
                "\nPlease download it from Firebase Console → Project Settings → Service Accounts " +
                "→ Generate new private key, rename it to 'firebase-service-account.json', " +
                "and place it in backend/src/main/resources/"
            );
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(databaseUrl)
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public DatabaseReference firebaseDatabaseRef(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp).getReference();
    }
}
