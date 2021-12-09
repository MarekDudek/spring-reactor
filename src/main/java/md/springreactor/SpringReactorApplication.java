package md.springreactor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.nCopies;

@SpringBootApplication
@Slf4j
public class SpringReactorApplication
{
    public static void main(String[] args)
    {
        Schedulers.enableMetrics();
        SpringApplication.run(SpringReactorApplication.class, args);
    }


    @Bean
    ApplicationRunner interval(TaskExecutor executor)
    {
        return args ->
                executor.execute(() ->
                        Flux.interval(ofSeconds(1)).
                                name("interval").
                                doOnNext(tick -> {
                                    try (MDC.MDCCloseable mdc = MDC.putCloseable("tick", Long.toString(tick)))
                                    {
                                        log.info("next {}", tick);
                                    }
                                }).
                                log().
                                metrics().
                                subscribe()
                );
    }

    @Bean
    ApplicationRunner someProcessing(TaskExecutor executor)
    {
        return args ->
                executor.execute(() ->
                        Flux.interval(ofSeconds(1)).
                                name("processing").
                                doOnNext(tick -> MDC.put("tick", Long.toString(tick))).
                                flatMap(tick -> {
                                            List<Long> longs = nCopies(tick.intValue(), 1L);
                                            return Flux.fromIterable(longs);
                                        }
                                ).
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

    @Bean
    public MeterBinder myBinder()
    {
        return registry ->
                Counter.builder("my-counter").
                        register(registry);
    }
}
