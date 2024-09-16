package com.blitzar.account_holder_id_document;

import io.micronaut.test.support.TestPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Testcontainers
public interface LocalStackTestContainer extends TestPropertyProvider {

    Logger logger = LoggerFactory.getLogger(LocalStackTestContainer.class);

    String localStackImageName = "localstack/localstack:3.7.0";
    DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse(localStackImageName);

    LocalStackContainer LOCALSTACK_CONTAINER = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(Service.S3)
            .withServices(Service.SQS)
            .withReuse(false);

    @Override
    default Map<String, String> getProperties() {
        Startables.deepStart(LOCALSTACK_CONTAINER).join();

        return Stream.of(getAWSProperties())
                .flatMap(property -> property.entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    default Map<String, String> getAWSProperties() {
        return Map.of(
                "AWS_ACCESS_KEY_ID", LOCALSTACK_CONTAINER.getAccessKey(),
                "AWS_SECRET_ACCESS_KEY", LOCALSTACK_CONTAINER.getSecretKey(),
                "AWS_DEFAULT_REGION", LOCALSTACK_CONTAINER.getRegion(),
                "AWS_S3_ENDPOINT", LOCALSTACK_CONTAINER.getEndpointOverride(Service.S3).toString(),
                "AWS_SQS_ENDPOINT", LOCALSTACK_CONTAINER.getEndpointOverride(Service.SQS).toString());
    }
}


