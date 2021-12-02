package md.springreactor;

import md.springreactor.greeting.GreetingClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringReactorApplication
{
    public static void main(String[] args)
    {
        ConfigurableApplicationContext context = SpringApplication.run(SpringReactorApplication.class, args);
        GreetingClient client = context.getBean(GreetingClient.class);
        System.out.println(">> message = " + client.getMessage().block());
    }
}
