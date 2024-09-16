package com.blitzar.account_holder_id_document.service;

import com.blitzar.account_holder_id_document.builder.S3ObjectKeyBuilder;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.objectstorage.aws.AwsS3Operations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;

@Singleton
public class SaveIdentityDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(SaveIdentityDocumentService.class);

    private AwsS3Operations awsS3Operations;

    @Inject
    public SaveIdentityDocumentService(AwsS3Operations awsS3Operations){
        this.awsS3Operations = awsS3Operations;
    }

    public UploadResponse<PutObjectResponse> saveFile(@NotBlank String accountHolderId, @NotNull CompletedFileUpload fileUpload){
        logger.info("[AccountHolderId={}, FileName={}] Attempting to save identification document file", accountHolderId, fileUpload.getFilename());

        if(StringUtils.isBlank(accountHolderId)){
            throw new IllegalArgumentException("accountHolderId can neither be null nor empty");
        }

        var fileExtension = FilenameUtils.getExtension(fileUpload.getFilename());
        var acceptedExtensionFiles = List.of(MediaType.IMAGE_PNG_TYPE.getExtension(), MediaType.IMAGE_JPEG_TYPE.getExtension());
        if(!acceptedExtensionFiles.contains(fileExtension)){
            throw new IllegalArgumentException("Only .png and .jpeg files are accepted");
        }

        var s3ObjectKey = S3ObjectKeyBuilder.build(accountHolderId, fileUpload.getFilename());

        var uploadRequest = UploadRequest.fromCompletedFileUpload(fileUpload, s3ObjectKey);
        var uploadResponse = awsS3Operations.upload(uploadRequest);

        logger.info("[AccountHolderId={}, FileName={}] Identity document uploaded", accountHolderId, fileUpload.getFilename());

        return uploadResponse;
    }
}
