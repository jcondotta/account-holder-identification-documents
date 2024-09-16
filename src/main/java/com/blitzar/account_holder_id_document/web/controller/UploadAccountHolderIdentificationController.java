package com.blitzar.account_holder_id_document.web.controller;

import com.blitzar.account_holder_id_document.service.UploadAccountHolderIdentityDocumentService;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.objectstorage.response.UploadResponse;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotNull;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Validated
@Controller(DocumentsAPIConstants.UPLOAD_ACCOUNT_HOLDER_IDENTIFICATION_API_V1_MAPPING)
@ExecuteOn(TaskExecutors.BLOCKING)
public class UploadAccountHolderIdentificationController {

    private final UploadAccountHolderIdentityDocumentService uploadIdDocumentService;

    public UploadAccountHolderIdentificationController(UploadAccountHolderIdentityDocumentService uploadIdDocumentService){
        this.uploadIdDocumentService = uploadIdDocumentService;
    }

    @Status(HttpStatus.CREATED)
    @Post(consumes = MediaType.MULTIPART_FORM_DATA)
    public HttpResponse<?> uploadIdentification(@PathVariable("account-holder-id") String accountHolderId,
                                                @NotNull CompletedFileUpload fileUpload,
                                                HttpRequest<?> request){

        UploadResponse<PutObjectResponse> response = uploadIdDocumentService.upload(accountHolderId, fileUpload);
        return HttpResponse
                .created(request.getUri())
                .header(HttpHeaders.ETAG, response.getETag());
    }
}
