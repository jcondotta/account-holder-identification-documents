#!/bin/sh

. /etc/localstack/init/ready.d/environment-variables/sqs-account-holder-identity-document-queue-settings.sh

awslocal sqs create-queue \
  --queue-name "${ACCOUNT_HOLDER_IDENTITY_DOCUMENT_QUEUE_NAME}"