package md.springreactor.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
@Slf4j
public class KafkaTemplateConfig
{
    @Autowired
    private KafkaProperties kafkaProperties;

    @Bean
    public ProducerFactory<String, String> producerFactory()
    {
        Map<String, Object> configs = new LinkedHashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configs.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate()
    {
        return new KafkaTemplate<>(producerFactory());
    }

    @Value("${md.kafka-template-config.topic-name}")
    private String topicName;

    @Bean
    public NewTopic newTopicCreatedByString()
    {
        return new NewTopic(topicName, 1, (short) 1);
    }

    @Scheduled(fixedRate = 1_000)
    public void sendMessage()
    {
        KafkaTemplate<String, String> template = kafkaTemplate();
        ListenableFuture<SendResult<String, String>> future = template.send(topicName, "my message");
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>()
                           {
                               @Override
                               public void onSuccess(SendResult<String, String> result)
                               {
                                   log.info("Send result: {}", result);
                               }

                               @Override
                               public void onFailure(Throwable exc)
                               {
                                   log.error("Send error", exc);
                               }
                           }
        );
    }
}
