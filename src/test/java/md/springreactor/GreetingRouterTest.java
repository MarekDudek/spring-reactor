package md.springreactor;


import md.springreactor.greeting.Greeting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class GreetingRouterTest
{
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testHello()
    {
        webTestClient.get().
                uri("/hello").accept(APPLICATION_JSON).
                exchange().
                expectStatus().isOk().
                expectBody(Greeting.class).
                value(greeting ->
                        assertThat(greeting.getMessage()).isEqualTo("Hello, Spring!")
                );
    }
}