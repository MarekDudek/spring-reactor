package md.springreactor.kafka;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import md.springreactor.kafka.docker.Compose;
import md.springreactor.kafka.docker.Renderer;
import md.springreactor.kafka.docker.Service;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Collections.singletonList;

@Disabled
public class DockerComposeConfigGenerationTest
{

    @Test
    void confluentPlatformKafka() throws IOException
    {
        String output = Renderer.render(compose());
        Files.write(Paths.get("./src/test/resources/docker/confluent-platform-generated.yaml"), output.getBytes());
        System.out.println(output);
    }

    private Compose compose()
    {
        return Compose.builder().
                version("2").
                services(ImmutableMap.<String, Service>builder().
                        put("zookeeper", zookeeper()).
                        put("broker", broker()).
                        put("schema-registry", schemaRegistry()).
                        build()).
                build();
    }

    private Service zookeeper()
    {
        return Service.builder().
                image("confluentinc/cp-zookeeper:7.0.1").
                hostname("zookeeper").
                container_name("zookeeper").
                ports(singletonList("2181:2181")).
                environment(ImmutableMap.of(
                        "ZOOKEEPER_CLIENT_PORT", 2181,
                        "ZOOKEEPER_TICK_TIME", 2000
                )).
                build();
    }

    private Service broker()
    {
        return Service.builder().
                image("confluentinc/cp-server:7.0.1").
                hostname("broker").
                container_name("broker").
                depends_on(singletonList("zookeeper")).
                ports(ImmutableList.of("9092:9092", "9101:9101")).
                environment(ImmutableMap.<String, Object>builder().
                        put("KAFKA_BROKER_ID", 1).
                        put("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181").
                        put("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT").
                        put("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://broker:29092,PLAINTEXT_HOST://localhost:9092").
                        put("KAFKA_METRIC_REPORTERS", "io.confluent.metrics.reporter.ConfluentMetricsReporter").
                        put("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", 1).
                        put("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", 0).
                        put("KAFKA_CONFLUENT_LICENSE_TOPIC_REPLICATION_FACTOR", 1).
                        put("KAFKA_CONFLUENT_BALANCER_TOPIC_REPLICATION_FACTOR", 1).
                        put("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", 1).
                        put("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", 1).
                        put("KAFKA_JMX_PORT", "9101").
                        put("KAFKA_JMX_HOSTNAME", "localhost").
                        put("KAFKA_CONFLUENT_SCHEMA_REGISTRY_URL", "http://schema-registry:8081").
                        put("CONFLUENT_METRICS_REPORTER_BOOTSTRAP_SERVERS", "broker:29092").
                        put("CONFLUENT_METRICS_REPORTER_TOPIC_REPLICAS", 1).
                        put("CONFLUENT_METRICS_ENABLE", "true").
                        put("CONFLUENT_SUPPORT_CUSTOMER_ID", "anonymous").
                        build()).
                build();
    }

    private Service schemaRegistry()
    {
        return Service.builder().
                image("confluentinc/cp-schema-registry:7.0.1").
                hostname("schema-registry").
                container_name("schema-registry").
                depends_on(singletonList("broker")).
                ports(singletonList("8081:8081")).
                environment(ImmutableMap.<String, Object>builder().
                        put("SCHEMA_REGISTRY_HOST_NAME", "schema-registry").
                        put("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "broker:29092").
                        put("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081").
                        build()).
                build();
    }
}

