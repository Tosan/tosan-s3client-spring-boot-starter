package com.tosan.client.s3.sample;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.MediaType;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

/**
 * @author saber mortazavi
 * @since 2026-01-10
 */
@SpringBootApplication
public class S3ClientApplication implements CommandLineRunner {

    private static final String bucketName = "bucketName";
    private final S3Client s3Client;

    public S3ClientApplication(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(S3ClientApplication.class).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        String key = save(new byte[1]);
        byte[] bytes = find(key);
        delete(key);
    }

    public String save(byte[] data) {
        String objectKey = generateObjectKey();
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(MediaType.IMAGE_JPEG_VALUE)
                    .contentLength((long) data.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(data));
            return objectKey;
        } catch (S3Exception e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] find(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
                return response.readAllBytes();
            }
        } catch (S3Exception | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void delete(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateObjectKey() {
        return UUID.randomUUID().toString();
    }
}
