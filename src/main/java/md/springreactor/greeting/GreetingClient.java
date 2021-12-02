package md.springreactor.greeting;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class GreetingClient
{
    private final WebClient client;

    public GreetingClient(WebClient.Builder builder)
    {
        client = builder.baseUrl("http://localhost:8080").build();
    }

    public Mono<String> getMessage()
    {
        return client.get().uri("/hello").accept(APPLICATION_JSON).
                retrieve().
                bodyToMono(Greeting.class).
                map(Greeting::getMessage);
    }
}
