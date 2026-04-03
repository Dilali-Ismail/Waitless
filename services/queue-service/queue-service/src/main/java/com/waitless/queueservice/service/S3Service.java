package com.waitless.queueservice.service;

import com.waitless.queueservice.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            log.warn("AWS S3: access-key/secret-key absents — upload de logo désactivé jusqu'à configuration.");
            return;
        }
        try {
            var creds = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey.trim(), secretKey.trim()));
            this.s3Client = S3Client.builder()
                    .region(Region.of(region.trim()))
                    .credentialsProvider(creds)
                    .build();
            this.s3Presigner = S3Presigner.builder()
                    .region(Region.of(region.trim()))
                    .credentialsProvider(creds)
                    .build();
            log.info("AWS S3 client initialisé (bucket={}, region={})", bucket, region);
        } catch (IllegalArgumentException e) {
            log.error("AWS S3: région invalide '{}'. Utilisez un code du type eu-west-3, eu-north-1.", region, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (s3Presigner != null) {
            s3Presigner.close();
        }
        if (s3Client != null) {
            s3Client.close();
        }
    }

    public boolean isConfigured() {
        return s3Client != null;
    }

    /**
     * URL affichable dans le navigateur. Bucket privé : l’URL HTTPS classique renvoie 403 dans
     * une image ; on renvoie une URL présignée (GET) à la place.
     */
    public String resolveLogoUrlForClient(String storedUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return null;
        }
        if (s3Presigner == null) {
            return storedUrl;
        }
        return extractObjectKey(storedUrl.trim())
                .map(this::presignGet)
                .orElse(storedUrl);
    }

    private java.util.Optional<String> extractObjectKey(String httpsUrl) {
        try {
            URI uri = URI.create(httpsUrl);
            String host = uri.getHost();
            if (host == null) {
                return java.util.Optional.empty();
            }
            String expectedPrefix = bucket + ".s3.";
            if (!host.startsWith(expectedPrefix) || !host.endsWith(".amazonaws.com")) {
                return java.util.Optional.empty();
            }
            String path = uri.getPath();
            if (path == null || path.length() <= 1) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(path.substring(1));
        } catch (Exception e) {
            log.debug("Impossible d'extraire la clé S3 depuis {}: {}", httpsUrl, e.getMessage());
            return java.util.Optional.empty();
        }
    }

    private String presignGet(String objectKey) {
        try {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(12))
                    .getObjectRequest(getReq)
                    .build();
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);
            return presigned.url().toExternalForm();
        } catch (SdkException e) {
            log.warn("Présignature GET logo échouée pour clé {}: {}", objectKey, e.getMessage());
            return null;
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (s3Client == null) {
            throw new BusinessException(
                    "Upload logo impossible : configurez AWS (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_S3_BUCKET, AWS_S3_REGION).");
        }
        String extension = getExtension(file.getOriginalFilename());
        String key = "logos/" + UUID.randomUUID() + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (SdkException e) {
            log.error("S3 putObject failed: {}", e.getMessage());
            String detail = e.getMessage();
            if (e instanceof AwsServiceException aws && aws.awsErrorDetails() != null
                    && aws.awsErrorDetails().errorMessage() != null) {
                detail = aws.awsErrorDetails().errorMessage();
            }
            throw new BusinessException("Échec envoi S3 : " + detail);
        }

        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region.trim(), key);
        log.info("File uploaded to S3: {}", url);
        return url;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        return filename.substring(filename.lastIndexOf("."));
    }
}