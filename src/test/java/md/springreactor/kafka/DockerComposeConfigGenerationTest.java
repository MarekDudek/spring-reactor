package md.springreactor.kafka;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import md.springreactor.kafka.docker.Compose;
import md.springreactor.kafka.docker.MultilineString;
import md.springreactor.kafka.docker.Service;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

import static java.lang.System.out;
import static java.util.Collections.singletonList;
import static md.springreactor.kafka.docker.Renderer.MAPPER;
import static md.springreactor.kafka.docker.Renderer.writeToString;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

@Disabled
public class DockerComposeConfigGenerationTest
{

    @Test
    void confluentPlatformKafka() throws IOException
    {
        Compose compose = compose();
        out.println(writeToString(compose));
        MAPPER.writeValue(new File("./src/test/resources/docker/confluent-platform-generated.yaml"), compose);
    }

    @Test
    void read() throws IOException
    {
        Compose compose = MAPPER.readValue(new File("./src/test/resources/docker/confluent-platform-generated.yaml"), Compose.class);
        out.println(compose);
    }

    @Test
    void snake() throws IOException
    {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(BLOCK);
        Yaml yaml = new Yaml(options);
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stream = classLoader.getResourceAsStream("docker/confluent-platform-orig.yml");
        Map<String, Object> map = yaml.load(stream);
        out.println(map);
        Writer output = new PrintWriter("./src/test/resources/docker/confluent-platform-generated.yaml");
        yaml.dump(map, output);
    }

    private Compose compose()
    {
        return Compose.builder().
                version("2").
                services(ImmutableMap.<String, Service>builder().
                        put("zookeeper", zookeeper()).
                        put("broker", broker()).
                        put("schema-registry", schemaRegistry()).
                        put("connect", connect()).
                        put("control-center", controlCenter()).
                        put("ksqldb-server", ksqldbServer()).
                        put("ksqldb-cli", ksqldbCli()).
                        put("ksql-datagen", ksqlDatagen()).
                        put("rest-proxy", restProxy()).
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
                        put("KAFKA_JMX_PORT", 9101).
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

    private Service connect()
    {
        return Service.builder().
                image("cnfldemos/cp-server-connect-datagen:0.5.0-6.2.0").
                hostname("connect").
                container_name("connect").
                depends_on(ImmutableList.of("broker", "schema-registry")).
                ports(singletonList("8083:8083")).
                environment(ImmutableMap.<String, Object>builder().
                        put("CONNECT_BOOTSTRAP_SERVERS", "broker:29092").
                        put("CONNECT_REST_ADVERTISED_HOST_NAME", "connect").
                        put("CONNECT_REST_PORT", 8083).
                        put("CONNECT_GROUP_ID", "compose-connect-group").
                        put("CONNECT_CONFIG_STORAGE_TOPIC", "docker-connect-configs").
                        put("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", 1).
                        put("CONNECT_OFFSET_FLUSH_INTERVAL_MS", 10000).
                        put("CONNECT_OFFSET_STORAGE_TOPIC", "docker-connect-offsets").
                        put("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", 1).
                        put("CONNECT_STATUS_STORAGE_TOPIC", "docker-connect-status").
                        put("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", 1).
                        put("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.storage.StringConverter").
                        put("CONNECT_VALUE_CONVERTER", "io.confluent.connect.avro.AvroConverter").
                        put("CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL", "http://schema-registry:8081").
                        put("CLASSPATH", "/usr/share/java/monitoring-interceptors/monitoring-interceptors-7.0.1.jar").
                        put("CONNECT_PRODUCER_INTERCEPTOR_CLASSES", "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor").
                        put("CONNECT_CONSUMER_INTERCEPTOR_CLASSES", "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor").
                        put("CONNECT_PLUGIN_PATH", "/usr/share/java,/usr/share/confluent-hub-components").
                        put("CONNECT_LOG4J_LOGGERS", "org.apache.zookeeper=ERROR,org.I0Itec.zkclient=ERROR,org.reflections=ERROR").
                        build()).
                build();
    }

    private Service controlCenter()
    {
        return Service.builder().
                image("confluentinc/cp-enterprise-control-center:7.0.1").
                hostname("control-center").
                container_name("control-center").
                depends_on(ImmutableList.of("broker", "schema-registry", "connect", "ksqldb-server")).
                ports(singletonList("9021:9021")).
                environment(ImmutableMap.<String, Object>builder().
                        put("CONTROL_CENTER_BOOTSTRAP_SERVERS", "broker:29092").
                        put("CONTROL_CENTER_CONNECT_CONNECT-DEFAULT_CLUSTER", "connect:8083").
                        put("CONTROL_CENTER_KSQL_KSQLDB1_URL", "http://ksqldb-server:8088").
                        put("CONTROL_CENTER_KSQL_KSQLDB1_ADVERTISED_URL", "http://localhost:8088").
                        put("CONTROL_CENTER_SCHEMA_REGISTRY_URL", "http://schema-registry:8081").
                        put("CONTROL_CENTER_REPLICATION_FACTOR", 1).
                        put("CONTROL_CENTER_INTERNAL_TOPICS_PARTITIONS", 1).
                        put("CONTROL_CENTER_MONITORING_INTERCEPTOR_TOPIC_PARTITIONS", 1).
                        put("CONFLUENT_METRICS_TOPIC_REPLICATION", 1).
                        put("PORT", 9021).
                        build()).
                build();
    }

    private Service ksqldbServer()
    {
        return Service.builder().
                image("confluentinc/cp-ksqldb-server:7.0.1").
                hostname("ksqldb-server").
                container_name("ksqldb-server").
                depends_on(ImmutableList.of("broker", "connect")).
                ports(singletonList("8088:8088")).
                environment(ImmutableMap.<String, Object>builder().
                        put("KSQL_CONFIG_DIR", "/etc/ksql").
                        put("KSQL_BOOTSTRAP_SERVERS", "broker:29092").
                        put("KSQL_HOST_NAME", "ksqldb-server").
                        put("KSQL_LISTENERS", "http://0.0.0.0:8088").
                        put("KSQL_CACHE_MAX_BYTES_BUFFERING", 0).
                        put("KSQL_KSQL_SCHEMA_REGISTRY_URL", "http://schema-registry:8081").
                        put("KSQL_PRODUCER_INTERCEPTOR_CLASSES", "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor").
                        put("KSQL_CONSUMER_INTERCEPTOR_CLASSES", "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor").
                        put("KSQL_KSQL_CONNECT_URL", "http://connect:8083").
                        put("KSQL_KSQL_LOGGING_PROCESSING_TOPIC_REPLICATION_FACTOR", 1).
                        put("KSQL_KSQL_LOGGING_PROCESSING_TOPIC_AUTO_CREATE", "true").
                        put("KSQL_KSQL_LOGGING_PROCESSING_STREAM_AUTO_CREATE", "true").
                        build()).
                build();
    }

    private Service ksqldbCli()
    {
        return Service.builder().
                image("confluentinc/cp-ksqldb-cli:7.0.1").
                container_name("ksqldb-cli").
                depends_on(ImmutableList.of("broker", "connect", "ksqldb-server")).
                entrypoint("/bin/sh").
                tty(true).
                build();
    }

    private Service ksqlDatagen()
    {
        return Service.builder().
                image("confluentinc/ksqldb-examples:7.0.1").
                hostname("ksql-datagen").
                container_name("ksql-datagen").
                depends_on(ImmutableList.of("ksqldb-server", "broker", "schema-registry", "connect")).
                command(MultilineString.builder().
                        lines(ImmutableList.<String>builder().
                                add("bash -c 'echo Waiting for Kafka to be ready...").
                                add("cub kafka-ready -b broker:29092 1 40").
                                add("echo Waiting for Confluent Schema Registry to be ready...").
                                add("cub sr-ready schema-registry 8081 40").
                                add("echo Waiting a few seconds for topic creation to finish...").
                                add("sleep 11").
                                add("tail -f /dev/null'").
                                build()).
                        build()).
                environment(ImmutableMap.<String, Object>builder().
                        put("KSQL_CONFIG_DIR", "/etc/ksql").
                        put("STREAMS_BOOTSTRAP_SERVERS", "broker:29092").
                        put("STREAMS_SCHEMA_REGISTRY_HOST", "schema-registry").
                        put("STREAMS_SCHEMA_REGISTRY_PORT", 8081).
                        build()).
                build();
    }

    private Service restProxy()
    {
        return Service.builder().
                image("confluentinc/cp-kafka-rest:7.0.1").
                depends_on(ImmutableList.of("broker", "schema-registry")).
                ports(singletonList("8082:8082")).
                hostname("rest-proxy").
                container_name("rest-proxy").
                environment(ImmutableMap.<String, Object>builder().
                        put("KAFKA_REST_HOST_NAME", "rest-proxy").
                        put("KAFKA_REST_BOOTSTRAP_SERVERS", "broker:29092").
                        put("KAFKA_REST_LISTENERS", "http://0.0.0.0:8082").
                        put("KAFKA_REST_SCHEMA_REGISTRY_URL", "http://schema-registry:8081").
                        build()).
                build();
    }
}

