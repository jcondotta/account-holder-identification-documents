package com.blitzar.account_holder_id_document.service;

import com.blitzar.account_holder_id_document.arguments.InvalidStringArgumentProvider;
import com.blitzar.account_holder_id_document.builder.S3ObjectKeyBuilder;
import com.blitzar.account_holder_id_document.web.events.AccountHolderIdentityDocumentEventProducer;
import com.blitzar.account_holder_id_document.web.events.UploadedIdentityDocumentMessage;
import io.micronaut.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UploadedIdentityDocumentNotifierTest {

    @InjectMocks
    private UploadedIdentityDocumentNotifier uploadedIdentityDocumentNotifier;

    @Mock
    private AccountHolderIdentityDocumentEventProducer eventProducer;

    private String accountHolderId = "6635471134";
    private String filenameWithoutExtension = "account-holder-identification-document";
    private String filenameWithExtension = filenameWithoutExtension + "." + MediaType.IMAGE_PNG;

    @BeforeEach
    public void beforeEach(){ }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG })
    public void givenValidMessage_whenNotify_thenSendSQSMessage(MediaType supportedMediaType) {
        var fileName = filenameWithoutExtension + "." + supportedMediaType.getExtension();

        var s3ObjectKey = S3ObjectKeyBuilder.build(accountHolderId, fileName);
        var uploadedIdentityDocumentMessage = new UploadedIdentityDocumentMessage(accountHolderId, s3ObjectKey);

        uploadedIdentityDocumentNotifier.sendMessage(uploadedIdentityDocumentMessage);

        verify(eventProducer).send(uploadedIdentityDocumentMessage);
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidStringArgumentProvider.class)
    public void givenInvalidAccountHolderId_whenNotify_thenThrowException(String invalidAccountHolderId){
        var s3ObjectKey = S3ObjectKeyBuilder.build(accountHolderId, filenameWithExtension);
        var uploadedIdentityDocumentMessage = new UploadedIdentityDocumentMessage(invalidAccountHolderId, s3ObjectKey);

        assertThatThrownBy(() -> uploadedIdentityDocumentNotifier.sendMessage(uploadedIdentityDocumentMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountHolderId can neither be null nor empty");

        verify(eventProducer, never()).send(uploadedIdentityDocumentMessage);
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidStringArgumentProvider.class)
    public void givenInvalidS3ObjectKey_whenNotify_thenThrowException(String invalidAccountHolderIdDocumentKey){
        var uploadedIdentityDocumentMessage = new UploadedIdentityDocumentMessage(accountHolderId, invalidAccountHolderIdDocumentKey);

        assertThatThrownBy(() -> uploadedIdentityDocumentNotifier.sendMessage(uploadedIdentityDocumentMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountHolderIdDocumentKey can neither be null nor empty");

        verify(eventProducer, never()).send(uploadedIdentityDocumentMessage);
    }
}