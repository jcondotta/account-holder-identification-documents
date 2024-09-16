package com.blitzar.account_holder_id_document.service;

import com.blitzar.account_holder_id_document.arguments.InvalidStringArgumentProvider;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.objectstorage.aws.AwsS3Operations;
import io.micronaut.objectstorage.request.UploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveIdentityDocumentServiceTest {

    @Mock
    private AwsS3Operations awsS3OperationsMock;

    @Mock
    private CompletedFileUpload completedFileUploadMock;

    private String accountHolderId = "6635471134";
    private String filenameWithoutExtension = "account-holder-identification-document";

    @InjectMocks
    private SaveIdentityDocumentService saveIdentityDocumentService;

    @BeforeEach
    public void beforeEach(){ }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG })
    public void givenSupportedMediaTypeFiles_whenSaveIdentityDocument_thenSuccess(MediaType supportedMediaType) {
        var fileName = filenameWithoutExtension + "." + supportedMediaType.getExtension();
        when(completedFileUploadMock.getFilename()).thenReturn(fileName);

        saveIdentityDocumentService.saveFile(accountHolderId, completedFileUploadMock);

        verify(awsS3OperationsMock).upload(any(UploadRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON, MediaType.APPLICATION_PDF, MediaType.IMAGE_GIF, MediaType.TEXT_PLAIN })
    public void givenUnsupportedMediaTypeFiles_whenSaveIdentityDocument_thenThrowException(MediaType unsupportedMediaType) {
        var fileName = filenameWithoutExtension + "." + unsupportedMediaType.getExtension();
        when(completedFileUploadMock.getFilename()).thenReturn(fileName);

        assertThatThrownBy(() -> saveIdentityDocumentService.saveFile(accountHolderId, completedFileUploadMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only .png and .jpeg files are accepted");

        verify(awsS3OperationsMock, never()).upload(any(UploadRequest.class));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidStringArgumentProvider.class)
    public void givenInvalidAccountHolderId_whenSaveIdDocumentFile_thenThrowException(String invalidAccountHolderId){
        assertThatThrownBy(() -> saveIdentityDocumentService.saveFile(invalidAccountHolderId, completedFileUploadMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountHolderId can neither be null nor empty");

        verify(awsS3OperationsMock, never()).upload(any(UploadRequest.class));
    }
}