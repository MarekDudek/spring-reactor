package md.springreactor.kafka.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;

public enum Renderer
{
    ;

    public static String render(Object object) throws JsonProcessingException
    {
        YAMLFactory factory =
                new YAMLFactory().
                        enable(MINIMIZE_QUOTES).
                        enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
        ObjectMapper mapper = new ObjectMapper(factory);
        return mapper.
                findAndRegisterModules().
                writerWithDefaultPrettyPrinter().
                writeValueAsString(object);
    }
}
