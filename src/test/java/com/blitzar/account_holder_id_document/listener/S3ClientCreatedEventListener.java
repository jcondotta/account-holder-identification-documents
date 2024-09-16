package com.blitzar.account_holder_id_document.listener;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

@Singleton
public class S3ClientCreatedEventListener implements BeanCreatedEventListener<S3Client> {

    private static final Logger logger = LoggerFactory.getLogger(S3ClientCreatedEventListener.class);

    @Value("${micronaut.object-storage.aws.account-holder-identity-document.bucket}")
    private String accountHolderIdentityDocumentBucketName;

    @Override
    public S3Client onCreated(@NonNull BeanCreatedEvent<S3Client> event) {
        var s3Client = event.getBean();

        logger.info("Creating S3 bucket with name: {}", accountHolderIdentityDocumentBucketName);
        s3Client.createBucket(builder -> builder.bucket(accountHolderIdentityDocumentBucketName).build());

        return s3Client;
    }
}