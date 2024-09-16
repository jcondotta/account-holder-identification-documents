package com.blitzar.account_holder_id_document.service;

import com.blitzar.account_holder_id_document.arguments.InvalidStringArgumentProvider;
import com.blitzar.account_holder_id_document.builder.S3ObjectKeyBuilder;
import com.blitzar.account_holder_id_document.web.events.UploadedIdentityDocumentMessage;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.objectstorage.response.UploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadAccountHolderIdentityDocumentServiceTest {

    @InjectMocks
    private UploadAccountHolderIdentityDocumentService uploadAccountHolderIdentityDocumentService;

    @Mock
    private SaveIdentityDocumentService saveIdentityDocumentService;

    @Mock
    private UploadedIdentityDocumentNotifier uploadedIdentityDocumentNotifier;

    @Mock
    private CompletedFileUpload completedFileUploadMock;

    @Mock
    private UploadResponse<PutObjectResponse> uploadResponseMock;

    private String accountHolderId = "6635471134";
    private String filenameWithoutExtension = "account-holder-identification-document";
    private String filenameWithExtension = filenameWithoutExtension + "." + MediaType.IMAGE_PNG;
    private String s3ObjectKey = accountHolderId + "/" + filenameWithExtension;

    @BeforeEach
    public void beforeEach(){ }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG })
    public void givenSupportedMediaTypeFiles_whenUploadAccountHolderIdentityDocument_thenSuccess(MediaType supportedMediaType) {
        var fileName = filenameWithoutExtension + "." + supportedMediaType.getExtension();
        var accountHolderIdDocumentKey = S3ObjectKeyBuilder.build(accountHolderId, fileName);

        when(uploadResponseMock.getKey()).thenReturn(accountHolderIdDocumentKey);
        when(saveIdentityDocumentService.saveFile(accountHolderId, completedFileUploadMock))
                .thenReturn(uploadResponseMock);

        uploadAccountHolderIdentityDocumentService.upload(accountHolderId, completedFileUploadMock);

        verify(saveIdentityDocumentService).saveFile(anyString(), any(CompletedFileUpload.class));
        verify(uploadedIdentityDocumentNotifier).sendMessage(any(UploadedIdentityDocumentMessage.class));
    }
}