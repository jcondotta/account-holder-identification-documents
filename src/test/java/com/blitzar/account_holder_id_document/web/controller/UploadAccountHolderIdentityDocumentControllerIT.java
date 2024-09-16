package com.blitzar.account_holder_id_document.web.controller;


import com.blitzar.account_holder_id_document.LocalStackTestContainer;
import com.blitzar.account_holder_id_document.builder.S3ObjectKeyBuilder;
import com.blitzar.account_holder_id_document.web.events.UploadedIdentityDocumentMessage;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.json.JsonMapper;
import io.micronaut.objectstorage.aws.AwsS3Operations;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest(transactional = false)
class UploadAccountHolderIdentityDocumentControllerIT implements LocalStackTestContainer {

    @Value("${aws.services.sqs.account-holder-identity-document-queue-name}")
    private String accountHolderIdentityDocumentQueueName;

    @Inject
    protected AwsS3Operations awsS3Operations;

    @Inject
    protected SqsClient sqsClient;

    @Inject
    private JsonMapper jsonMapper;

    @Inject
    private RequestSpecification requestSpecification;

    private String accountHolderId = "6635471134";
    private String filenameWithoutExtension = "account-holder-identification-document";
    private String accountHolderIdentityDocumentQueueURL;

    @BeforeAll
    public static void beforeAll(){
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    public void beforeEach(RequestSpecification requestSpecification) {
        this.accountHolderIdentityDocumentQueueURL = sqsClient.getQueueUrl(builder -> builder.queueName(accountHolderIdentityDocumentQueueName)).queueUrl();
        this.requestSpecification = requestSpecification
                .contentType(ContentType.MULTIPART)
                .basePath(DocumentsAPIConstants.UPLOAD_ACCOUNT_HOLDER_IDENTIFICATION_API_V1_MAPPING);
    }

    public File createTempFile(MediaType mediaType) throws IOException {
        var path = Files.createTempFile("test-file-", ".".concat(mediaType.getExtension()));
        return path.toFile();
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG })
    void givenSupportedMediaTypeFiles_whenUploadAccountHolderIdentityDocument_thenUploadFile(MediaType supportedMediaType) throws IOException {
        File file = createTempFile(supportedMediaType);

        given()
            .spec(requestSpecification)
                .multiPart("fileUpload", file)
                .pathParam("account-holder-id", accountHolderId)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.getCode())
                .header("location", equalTo("/api/v1/account-holders/account-holder-id/6635471134/upload-identity-document"))
                .header("ETag", notNullValue());

        var expectedS3ObjectKey = S3ObjectKeyBuilder.build(accountHolderId, file.getName());
        Assertions.assertThat(awsS3Operations.exists(expectedS3ObjectKey)).isTrue();

        await().pollDelay(1, TimeUnit.SECONDS).untilAsserted(() -> {
            var receiveMessageResponse = sqsClient.receiveMessage(builder -> builder.queueUrl(accountHolderIdentityDocumentQueueURL).build());
            assertThat(receiveMessageResponse.messages().size()).isEqualTo(1);

            var message = receiveMessageResponse.messages().get(0);
            var uploadedIdentityDocumentMessage = jsonMapper.readValue(message.body(), UploadedIdentityDocumentMessage.class);

            assertThat(uploadedIdentityDocumentMessage.accountHolderId()).isEqualTo(accountHolderId);
            assertThat(uploadedIdentityDocumentMessage.accountHolderIdDocumentKey()).isEqualTo(expectedS3ObjectKey);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON, MediaType.APPLICATION_PDF, MediaType.IMAGE_GIF, MediaType.TEXT_PLAIN })
    void givenUnsupportedMediaTypeFiles_whenUploadAccountHolderIdentityDocument_thenReturnBadRequest(MediaType unsupportedMediaType) throws IOException {
        File file = createTempFile(unsupportedMediaType);

        given()
            .spec(requestSpecification)
                .multiPart("fileUpload", file)
                .pathParam("account-holder-id", accountHolderId)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.getCode())
            .rootPath("_embedded")
                .body("errors", hasSize(1))
                .body("errors[0].message", equalTo("Only .png and .jpeg files are accepted"));


        var nonExistentS3ObjectKey = S3ObjectKeyBuilder.build(accountHolderId, file.getName());
        Assertions.assertThat(awsS3Operations.exists(nonExistentS3ObjectKey)).isFalse();

        await().pollDelay(1, TimeUnit.SECONDS).untilAsserted(() -> {
            var receiveMessageResponse = sqsClient.receiveMessage(builder -> builder.queueUrl(accountHolderIdentityDocumentQueueURL).build());
            assertThat(receiveMessageResponse.messages().size()).isEqualTo(0);
        });
    }
}
