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
                services(ImmutableMap.of(
                        "zookeeper", zookeeper(),
                        "broker", broker()
                )).
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
                build();
    }
}

