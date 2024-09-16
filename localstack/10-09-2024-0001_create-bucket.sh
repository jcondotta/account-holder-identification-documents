#!/bin/sh

. /etc/localstack/init/ready.d/environment-variables/s3-account-holder-identity-document-bucket-settings.sh

awslocal s3api create-bucket \
  --bucket "${ACCOUNT_HOLDER_IDENTITY_DOCUMENT_BUCKET_NAME}"