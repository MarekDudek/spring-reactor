package md.springreactor.kafka.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;

public enum Renderer
{
    ;


    private static final YAMLFactory FACTORY =
            new YAMLFactory().
                    enable(MINIMIZE_QUOTES).
                    enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS);

    public static final ObjectMapper MAPPER =
            new ObjectMapper(FACTORY);

    public static String writeToString(Object object) throws JsonProcessingException
    {
        return MAPPER.
                findAndRegisterModules().
                writerWithDefaultPrettyPrinter().
                writeValueAsString(object);
    }

    public static void read() {
    }
}
