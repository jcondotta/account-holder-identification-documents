package com.blitzar.account_holder_id_document.web.events;

import io.micronaut.jms.annotations.JMSProducer;
import io.micronaut.jms.annotations.Queue;
import io.micronaut.jms.sqs.configuration.SqsConfiguration;
import io.micronaut.messaging.annotation.MessageBody;

@JMSProducer(SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME)
public interface AccountHolderIdentityDocumentEventProducer {

    @Queue("${aws.services.sqs.account-holder-identity-document-queue-name}")
    void send(@MessageBody UploadedIdentityDocumentMessage uploadedIdentityDocumentMessage);
}
