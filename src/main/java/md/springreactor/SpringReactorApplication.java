package md.springreactor;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import static java.time.Duration.ofSeconds;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class SpringReactorApplication
{
    public static void main(String[] args)
    {
        Schedulers.enableMetrics();
        SpringApplication.run(SpringReactorApplication.class, args);
    }


    @Bean
    ApplicationRunner interval(TaskExecutor taskExecutor)
    {
        return args ->
                taskExecutor.execute(() ->
                        Flux.interval(ofSeconds(1)).
                                name("interval").
                                log().
                                metrics().
                                subscribe()
                );
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags()
    {
        return registry -> registry.config().commonTags("application", "demo");
    }
}
