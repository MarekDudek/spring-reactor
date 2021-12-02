package md.springreactor.greeting;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class GreetingHandler
{
    public Mono<ServerResponse> hello()
    {
        return ServerResponse.ok().
                contentType(APPLICATION_JSON).
                body(fromValue(
                        new Greeting("Hello, Spring!"))
                );
    }
}
