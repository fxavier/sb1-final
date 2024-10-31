package com.ecommerce.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class ImageStorageService {
    
    @ConfigProperty(name = "quarkus.s3.aws.region")
    String awsRegion;
    
    @ConfigProperty(name = "quarkus.s3.aws.credentials.access-key-id")
    String accessKeyId;
    
    @ConfigProperty(name = "quarkus.s3.aws.credentials.secret-access-key")
    String secretAccessKey;
    
    @ConfigProperty(name = "quarkus.s3.bucket")
    String bucket;
    
    private S3Client getS3Client() {
        return S3Client.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .build();
    }
    
    public Uni<String> uploadImage(String fileName, InputStream inputStream, String contentType, long size) {
        return Uni.createFrom().item(() -> {
            try {
                S3Client s3Client = getS3Client();
                
                // Ensure bucket exists
                if (!bucketExists(s3Client, bucket)) {
                    createBucket(s3Client, bucket);
                }
                
                // Generate unique file name
                String uniqueFileName = UUID.randomUUID().toString() + 
                    "_" + fileName.replaceAll("\\s+", "_");
                
                // Upload file
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(uniqueFileName)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
                
                s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(inputStream, size));
                
                // Return the URL
                return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucket, awsRegion, uniqueFileName);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image to S3", e);
            }
        });
    }
    
    public Uni<Void> deleteImage(String imageUrl) {
        return Uni.createFrom().item(() -> {
            try {
                S3Client s3Client = getS3Client();
                
                // Extract key from URL
                String key = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
                
                s3Client.deleteObject(deleteObjectRequest);
                
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete image from S3", e);
            }
        });
    }
    
    private boolean bucketExists(S3Client s3Client, String bucket) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                .bucket(bucket)
                .build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
    
    private void createBucket(S3Client s3Client, String bucket) {
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
            .bucket(bucket)
            .acl(BucketCannedACL.PUBLIC_READ)
            .build();
        
        s3Client.createBucket(createBucketRequest);
        
        // Enable public access
        PublicAccessBlockConfiguration publicAccessConfig = 
            PublicAccessBlockConfiguration.builder()
                .blockPublicAcls(false)
                .blockPublicPolicy(false)
                .ignorePublicAcls(false)
                .restrictPublicBuckets(false)
                .build();
        
        PutPublicAccessBlockRequest publicAccessRequest = 
            PutPublicAccessBlockRequest.builder()
                .bucket(bucket)
                .publicAccessBlockConfiguration(publicAccessConfig)
                .build();
        
        s3Client.putPublicAccessBlock(publicAccessRequest);
        
        // Add bucket policy for public read
        String bucketPolicy = String.format(
            "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Sid\":\"PublicReadGetObject\"," +
            "\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"s3:GetObject\"," +
            "\"Resource\":\"arn:aws:s3:::%s/*\"}]}", bucket);
        
        PutBucketPolicyRequest policyRequest = PutBucketPolicyRequest.builder()
            .bucket(bucket)
            .policy(bucketPolicy)
            .build();
        
        s3Client.putBucketPolicy(policyRequest);
    }
}