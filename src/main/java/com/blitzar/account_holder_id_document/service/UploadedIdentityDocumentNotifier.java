package com.blitzar.account_holder_id_document.service;

import com.blitzar.account_holder_id_document.web.events.AccountHolderIdentityDocumentEventProducer;
import com.blitzar.account_holder_id_document.web.events.UploadedIdentityDocumentMessage;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UploadedIdentityDocumentNotifier {

    private static final Logger logger = LoggerFactory.getLogger(UploadedIdentityDocumentNotifier.class);
    private final AccountHolderIdentityDocumentEventProducer eventProducer;

    public UploadedIdentityDocumentNotifier(AccountHolderIdentityDocumentEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    public void sendMessage(@NotNull UploadedIdentityDocumentMessage uploadedIdentityDocumentMessage){
        logger.info("[AccountHolderId={}] Putting a message on SQS Queue due identity document upload", uploadedIdentityDocumentMessage.accountHolderId());

        if(StringUtils.isBlank(uploadedIdentityDocumentMessage.accountHolderId())){
            throw new IllegalArgumentException("accountHolderId can neither be null nor empty");
        }

        if(StringUtils.isBlank(uploadedIdentityDocumentMessage.accountHolderIdDocumentKey())){
            throw new IllegalArgumentException("accountHolderIdDocumentKey can neither be null nor empty");
        }

        eventProducer.send(uploadedIdentityDocumentMessage);
        logger.info("[AccountHolderId={}] Message sent", uploadedIdentityDocumentMessage.accountHolderId());
    }
}
