package com.blitzar.account_holder_id_document.factory;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Factory
public class SQSClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(SQSClientFactory.class);

    @Singleton
    @Replaces(SqsClient.class)
    @Requires(property = "aws.services.sqs.endpoint-override", pattern = "^$")
    public SqsClient sqsClient(AwsCredentials awsCredentials, Region region){
        logger.info("Building SQSClient with params: awsCredentials: {} and region: {}", awsCredentials, region);
        return SqsClient.builder()
                .region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    @Singleton
    @Replaces(SqsClient.class)
    @Requires(property = "aws.services.sqs.endpoint-override", pattern = "(.|\\s)*\\S(.|\\s)*")
    public SqsClient sqsClientEndpointOverridden(AwsCredentials awsCredentials, Region region,
                                                 @Value("${aws.services.sqs.endpoint-override}") String endpoint){

        logger.info("Building SQSClient with params: awsCredentials: {}, region: {} and endpoint: {}", awsCredentials, region, endpoint);

        return SqsClient.builder()
                .region(region)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}