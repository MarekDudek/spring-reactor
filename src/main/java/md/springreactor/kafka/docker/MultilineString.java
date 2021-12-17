package md.springreactor.kafka.docker;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MultilineString
{
    List<String> lines;
}
