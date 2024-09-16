package com.blitzar.account_holder_id_document.builder;

public final class S3ObjectKeyBuilder {

    public static String build(String accountHolderId, String fileUploadFilename) {
        return accountHolderId + "/" + fileUploadFilename;
    }
}