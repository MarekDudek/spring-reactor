package md.springreactor.kafka;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;

public class DockerComposeConfigGenerationTest
{
    @Test
    void test() throws IOException
    {
        Map<String, Object> confluentKafkaPlatform = new LinkedHashMap<>();
        confluentKafkaPlatform.put("version", "2");

        Map<String, Object> services = new LinkedHashMap<>();
        LinkedHashMap<Object, Object> zookeeper = new LinkedHashMap<>();
        zookeeper.put("image", "confluentinc/cp-zookeeper:7.0.1");
        zookeeper.put("hostname", "zookeeper");
        zookeeper.put("container_name", "zookeeper");
        ArrayList<Object> ports = new ArrayList<>();
        ports.add("2181:2181");
        zookeeper.put("ports", ports);
        LinkedHashMap<Object, Object> environment = new LinkedHashMap<>();
        environment.put("ZOOKEEPER_CLIENT_PORT", 2181);
        environment.put("ZOOKEEPER_TICK_TIME", 2000);
        zookeeper.put("environment", environment);
        services.put("zookeeper", zookeeper);
        //services.put("broker", new LinkedHashMap<>());

        confluentKafkaPlatform.put("services", services);


        YAMLFactory factory = new YAMLFactory().enable(MINIMIZE_QUOTES).enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.findAndRegisterModules();
        String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(confluentKafkaPlatform);
        Files.write(Paths.get("./zookeeper.yaml"), output.getBytes());
        System.out.println(output);
    }
}
