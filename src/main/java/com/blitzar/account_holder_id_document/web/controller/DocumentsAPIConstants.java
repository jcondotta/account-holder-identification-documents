package com.blitzar.account_holder_id_document.web.controller;

public interface DocumentsAPIConstants {

    String BASE_PATH_API_V1_MAPPING = "/api/v1/account-holders";
    String ACCOUNT_HOLDER_API_V1_MAPPING = BASE_PATH_API_V1_MAPPING + "/account-holder-id/{account-holder-id}";
    String UPLOAD_ACCOUNT_HOLDER_IDENTIFICATION_API_V1_MAPPING = ACCOUNT_HOLDER_API_V1_MAPPING + "/upload-identity-document";

}
