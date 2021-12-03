package md.springreactor;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import static java.time.Duration.ofMillis;

@SpringBootApplication
public class SpringReactorApplication
{
    public static void main(String[] args)
    {
        Schedulers.enableMetrics();
        ConfigurableApplicationContext context = SpringApplication.run(SpringReactorApplication.class, args);
    }


    @Bean
    ApplicationRunner interval(TaskExecutor executor)
    {
        return args ->
                executor.execute(() ->
                        Flux.interval(ofMillis(1)).
                                name("interval").
                                metrics().
                                log().
                                subscribe()
                );
    }
}
