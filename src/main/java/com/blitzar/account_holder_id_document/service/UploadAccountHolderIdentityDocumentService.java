package com.blitzar.account_holder_id_document.service;

import com.blitzar.account_holder_id_document.web.events.UploadedIdentityDocumentMessage;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Singleton
public class UploadAccountHolderIdentityDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(UploadAccountHolderIdentityDocumentService.class);

    private final SaveIdentityDocumentService saveIdentityDocumentService;
    private final UploadedIdentityDocumentNotifier uploadedIdentityDocumentNotifier;

    @Inject
    public UploadAccountHolderIdentityDocumentService(SaveIdentityDocumentService saveIdentityDocumentService, UploadedIdentityDocumentNotifier uploadedIdentityDocumentNotifier) {
        this.saveIdentityDocumentService = saveIdentityDocumentService;
        this.uploadedIdentityDocumentNotifier = uploadedIdentityDocumentNotifier;
    }

    public UploadResponse<PutObjectResponse> upload(@NotBlank String accountHolderId, @NotNull CompletedFileUpload fileUpload){
        UploadResponse<PutObjectResponse> uploadResponse = saveIdentityDocumentService.saveFile(accountHolderId, fileUpload);

        var uploadedIdDocumentMessage = new UploadedIdentityDocumentMessage(accountHolderId, uploadResponse.getKey());
        uploadedIdentityDocumentNotifier.sendMessage(uploadedIdDocumentMessage);

        return uploadResponse;
    }
}
