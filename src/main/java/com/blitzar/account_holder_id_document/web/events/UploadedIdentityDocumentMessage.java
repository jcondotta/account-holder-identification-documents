package com.blitzar.account_holder_id_document.web.events;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record UploadedIdentityDocumentMessage(
        @NotBlank(message = "accountHolder.accountHolderId.notBlank") String accountHolderId,
        @NotBlank(message = "accountHolder.idDocument.key.notBlank") String accountHolderIdDocumentKey) {

}