package md.springreactor.kafka.docker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import static java.util.stream.Collectors.joining;

public class MultilineStringSerializer extends StdSerializer<MultilineString>
{
    public MultilineStringSerializer()
    {
        this(null);
    }

    public MultilineStringSerializer(Class<MultilineString> t)
    {
        super(t);
    }

    @Override
    public void serialize(MultilineString value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {
        String string = value.getLines().stream().collect(joining(" && "));
        gen.writeString(string);
    }
}
