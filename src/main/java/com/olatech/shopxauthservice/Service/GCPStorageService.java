package com.olatech.shopxauthservice.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.storage.BlobId;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GCPStorageService {

    private final Storage storage;

    @Value("${gcp.storage.bucket}")
    private String bucketName;

    @Value("${gcp.storage.credentials.path}")
    private String credentialsPath;

    public GCPStorageService(@Value("${gcp.storage.credentials.path:}") String credentialsPath,
                             ResourceLoader resourceLoader) throws IOException {
        StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder();

        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            try {
                // Charger la ressource à partir du classpath
                Resource resource = resourceLoader.getResource(credentialsPath);

                if (resource.exists()) {
                    // Utiliser un InputStream au lieu d'un FileInputStream pour les ressources classpath
                    GoogleCredentials credentials = GoogleCredentials.fromStream(
                            resource.getInputStream()
                    );
                    optionsBuilder.setCredentials(credentials);
                } else {
                    throw new IOException("Le fichier de credentials n'existe pas: " + credentialsPath);
                }
            } catch (Exception e) {
                throw new IOException("Impossible de charger les credentials depuis " + credentialsPath, e);
            }
        } else {
            // Si aucun chemin n'est spécifié, utiliser les credentials par défaut
            this.storage = StorageOptions.getDefaultInstance().getService();
            return;
        }

        this.storage = optionsBuilder.build().getService();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        // Téléchargement du fichier
        storage.create(blobInfo, file.getBytes());


        // Génération de l'URL publique
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }

    // Méthode alternative pour générer une URL signée à durée limitée
    public String generateSignedUrl(String objectName, long expirationTimeInSeconds) {
        BlobId blobId = BlobId.of(bucketName, objectName);

        return storage.signUrl(
                BlobInfo.newBuilder(blobId).build(),
                expirationTimeInSeconds,
                TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature()
        ).toString();
    }
}