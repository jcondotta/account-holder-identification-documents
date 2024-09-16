package com.blitzar.account_holder_id_document.listener;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;

@Singleton
public class SqsClientCreatedEventListener implements BeanCreatedEventListener<SqsClient> {

    private static final Logger logger = LoggerFactory.getLogger(SqsClientCreatedEventListener.class);

    @Value("${aws.services.sqs.account-holder-identity-document-queue-name}")
    private String accountHolderIdentityDocumentQueueName;

    @Override
    public SqsClient onCreated(@NonNull BeanCreatedEvent<SqsClient> event) {
        var sqsClient = event.getBean();

        if (sqsClient.listQueues().queueUrls().stream().noneMatch(queueURL -> queueURL.contains(accountHolderIdentityDocumentQueueName))) {
            logger.info("Creating queue with name: {}", accountHolderIdentityDocumentQueueName, sqsClient.serviceClientConfiguration());
            sqsClient.createQueue(builder -> builder.queueName(accountHolderIdentityDocumentQueueName).build());
        }
        return sqsClient;
    }
}