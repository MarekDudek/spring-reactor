package md.springreactor.kafka.docker;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultilineString
{
    List<String> lines;
}
