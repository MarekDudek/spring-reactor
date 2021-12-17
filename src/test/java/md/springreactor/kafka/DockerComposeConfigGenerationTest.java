package md.springreactor.kafka;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

@Disabled
public class DockerComposeConfigGenerationTest
{

    private static final DumperOptions OPTIONS = new DumperOptions();

    static
    {
        OPTIONS.setPrettyFlow(true);
        OPTIONS.setDefaultFlowStyle(BLOCK);
    }

    private static final Yaml YAML = new Yaml(OPTIONS);
    private static final Gson GSON = new GsonBuilder().
            registerTypeAdapter(new TypeToken<Map<String, Object>>()
            {
            }.getType(), new DoubleToIntDeserializer()).
            create();

    @Test
    void original_to_equivalent() throws IOException
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("docker/confluent-platform-orig.yaml");
        Map<String, Object> compose = YAML.load(stream);
        Writer output = new PrintWriter("./src/test/resources/docker/confluent-platform-equiv.yaml");
        YAML.dump(compose, output);
    }

    @Test
    void multiply_brokers() throws FileNotFoundException
    {
        int count = 5;

        InputStream stream = getClass().getClassLoader().getResourceAsStream("docker/confluent-platform-equiv.yaml");
        Map<String, Object> compose = YAML.load(stream);

        Map<String, Object> services = (Map<String, Object>) compose.get("services");

        String bootstrapServers = rangeClosed(1, count).boxed().map(i -> "broker-" + i + ":9092").collect(joining(","));

        Map<String, Object> zookeeper = (Map<String, Object>) services.get("zookeeper");
        services.remove("zookeeper");
        services.put("zookeeper", zookeeper);

        Map<String, Object> oldBroker = (Map<String, Object>) services.get("broker");
        services.remove("broker");

        Map<String, Map<String, Object>> brokers =
                rangeClosed(1, count).boxed().map(number -> {

                            Map<String, Object> broker = new LinkedHashMap<>();

                            broker.put("image", oldBroker.get("image"));
                            broker.put("hostname", oldBroker.get("hostname"));
                            broker.put("container_name", oldBroker.get("container_name"));

                            List<String> oldDependsOn = (List<String>) oldBroker.get("depends_on");
                            List<Object> dependsOn = new ArrayList<>(oldDependsOn);
                            broker.put("depends_on", dependsOn);

                            List<String> oldPorts = (List<String>) oldBroker.get("ports");
                            List<Object> ports = new ArrayList<>(oldPorts);
                            broker.put("ports", ports);

                            Map<String, Object> oldEnvironment = (Map<String, Object>) oldBroker.get("environment");
                            Map<String, Object> environment = new LinkedHashMap<>(oldEnvironment);
                            broker.put("environment", environment);

                            String name = "broker-" + number;
                            broker.replace("hostname", name);
                            broker.replace("container_name", name);
                            environment = (Map<String, Object>) broker.get("environment");
                            environment.replace("KAFKA_BROKER_ID", number);
                            environment.replace("CONFLUENT_METRICS_REPORTER_BOOTSTRAP_SERVERS", bootstrapServers);

                            return broker;
                        }
                ).collect(toMap(
                                broker ->
                                        (String) broker.get("hostname"),
                                identity(),
                                (a, b) -> b,
                                LinkedHashMap::new
                        )
                );
        services.putAll(brokers);

        Map<String, Object> schemaRegistry = (Map<String, Object>) services.get("schema-registry");
        services.remove("schema-registry");
        services.put("schema-registry", schemaRegistry);
        Map<String, Object> environment = (Map<String, Object>) schemaRegistry.get("environment");
        environment.replace("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", bootstrapServers);
        List<String> dependsOn = (List<String>) schemaRegistry.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> connect = (Map<String, Object>) services.get("connect");
        services.remove("connect");
        services.put("connect", connect);
        environment = (Map<String, Object>) connect.get("environment");
        environment.replace("CONNECT_BOOTSTRAP_SERVERS", bootstrapServers);
        dependsOn = (List<String>) connect.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> controlCenter = (Map<String, Object>) services.get("control-center");
        services.remove("control-center");
        services.put("control-center", controlCenter);
        environment = (Map<String, Object>) controlCenter.get("environment");
        environment.replace("CONTROL_CENTER_BOOTSTRAP_SERVERS", bootstrapServers);
        dependsOn = (List<String>) controlCenter.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> ksqldbServer = (Map<String, Object>) services.get("ksqldb-server");
        services.remove("ksqldb-server");
        services.put("ksqldb-server", ksqldbServer);
        environment = (Map<String, Object>) ksqldbServer.get("environment");
        environment.replace("KSQL_BOOTSTRAP_SERVERS", bootstrapServers);
        dependsOn = (List<String>) ksqldbServer.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> ksqldbCli = (Map<String, Object>) services.get("ksqldb-cli");
        services.remove("ksqldb-cli");
        services.put("ksqldb-cli", ksqldbCli);
        dependsOn = (List<String>) ksqldbCli.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> ksqlDatagen = (Map<String, Object>) services.get("ksql-datagen");
        services.remove("ksql-datagen");
        services.put("ksql-datagen", ksqlDatagen);
        environment = (Map<String, Object>) ksqlDatagen.get("environment");
        environment.replace("STREAMS_BOOTSTRAP_SERVERS", bootstrapServers);
        dependsOn = (List<String>) ksqlDatagen.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> restProxy = (Map<String, Object>) services.get("rest-proxy");
        services.remove("rest-proxy");
        services.put("rest-proxy", restProxy);
        environment = (Map<String, Object>) restProxy.get("environment");
        environment.replace("KAFKA_REST_BOOTSTRAP_SERVERS", bootstrapServers);
        dependsOn = (List<String>) restProxy.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Writer output = new PrintWriter("./src/test/resources/docker/confluent-platform-multi-broker.yaml");
        YAML.dump(compose, output);
    }
}

