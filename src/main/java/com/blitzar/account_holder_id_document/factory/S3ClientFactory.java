package com.blitzar.account_holder_id_document.factory;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Factory
public class S3ClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(S3ClientFactory.class);

    @Singleton
    @Replaces(S3Client.class)
    @Requires(property = "aws.services.s3.endpoint-override", pattern = "^$")
    public S3Client s3Client(AwsCredentials awsCredentials, Region region){
        logger.info("Building S3Client with params: awsCredentials: {} and region: {}", awsCredentials, region);

        return S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    @Singleton
    @Replaces(S3Client.class)
    @Requires(property = "aws.services.s3.endpoint-override", pattern = "(.|\\s)*\\S(.|\\s)*")
    public S3Client s3ClientEndpointOverridden(AwsCredentials awsCredentials, Region region,
                                               @Value("${aws.services.s3.endpoint-override}") String endpoint){

        logger.info("Building S3Client with params: awsCredentials: {}, region: {} and endpoint: {}", awsCredentials, region, endpoint);

        return S3Client.builder()
                .region(region)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
