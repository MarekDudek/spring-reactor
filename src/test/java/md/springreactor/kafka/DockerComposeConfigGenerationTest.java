package md.springreactor.kafka;

import com.google.gson.Gson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
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
    private static final Gson GSON = new Gson();

    @Test
    void original_to_equivalent() throws IOException
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("docker/confluent-platform-orig.yml");
        Map<String, Object> compose = YAML.load(stream);
        Writer output = new PrintWriter("./src/test/resources/docker/confluent-platform-equiv.yaml");
        YAML.dump(compose, output);
    }

    @Test
    void multiply_brokers() throws FileNotFoundException
    {
        int count = 5;

        InputStream stream = getClass().getClassLoader().getResourceAsStream("docker/confluent-platform-orig.yml");
        Map<String, Object> compose = YAML.load(stream);

        Map<String, Object> deepCopy = GSON.fromJson(GSON.toJson(compose), Map.class);
        Map<String, Object> services = (Map<String, Object>) deepCopy.get("services");

        Map<String, Object> oldBroker = (Map<String, Object>) services.get("broker");
        services.remove("broker");

        Map<String, Map<String, Object>> brokers =
                rangeClosed(1, count).boxed().map(number -> {
                            Map<String, Object> broker = GSON.fromJson(GSON.toJson(oldBroker), Map.class);
                            String name = "broker-" + number;
                            broker.replace("hostname", name);
                            broker.replace("container_name", name);
                            Map<String, Object> environment = (Map<String, Object>) broker.get("environment");
                            environment.replace("KAFKA_BROKER_ID", number);
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
        List<String> dependsOn = (List<String>) schemaRegistry.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> connect = (Map<String, Object>) services.get("connect");
        services.remove("connect");
        services.put("connect", connect);
        dependsOn = (List<String>) connect.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> controlCenter = (Map<String, Object>) services.get("control-center");
        services.remove("control-center");
        services.put("control-center", controlCenter);
        dependsOn = (List<String>) controlCenter.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> ksqldbServer = (Map<String, Object>) services.get("ksqldb-server");
        services.remove("ksqldb-server");
        services.put("ksqldb-server", ksqldbServer);
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
        dependsOn = (List<String>) ksqlDatagen.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Map<String, Object> restProxy = (Map<String, Object>) services.get("rest-proxy");
        services.remove("rest-proxy");
        services.put("rest-proxy", restProxy);
        dependsOn = (List<String>) restProxy.get("depends_on");
        dependsOn.remove("broker");
        dependsOn.addAll(brokers.keySet());

        Writer output = new PrintWriter("./src/test/resources/docker/confluent-platform-multi-broker.yaml");
        YAML.dump(deepCopy, output);
    }
}

