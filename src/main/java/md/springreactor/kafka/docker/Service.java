package md.springreactor.kafka.docker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
public class Service
{
    String image;
    String hostname;
    String container_name;
    List<String> depends_on;
    List<String> ports;
    Map<String, Object> environment;
    String entrypoint;
    Boolean tty;
    @JsonSerialize(using = MultilineStringSerializer.class)
    MultilineString command;
}
