package md.springreactor.kafka;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DoubleToIntDeserializer implements JsonDeserializer<Map<String, Object>>
{
    @Override
    public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return (Map<String, Object>) read(json);
    }

    public Object read(JsonElement jsonElement)
    {
        if (jsonElement.isJsonArray())
        {
            List<Object> list = new ArrayList<>();
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement anArr : jsonArray)
                list.add(read(anArr));
            return list;
        }
        if (jsonElement.isJsonObject())
        {
            Map<String, Object> map = new LinkedTreeMap<>();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entitySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entitySet)
                map.put(entry.getKey(), read(entry.getValue()));
            return map;
        }
        if (jsonElement.isJsonPrimitive())
        {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isBoolean())
                return jsonPrimitive.getAsBoolean();
            if (jsonPrimitive.isString())
                return jsonPrimitive.getAsString();
            if (jsonPrimitive.isNumber())
            {
                Number num = jsonPrimitive.getAsNumber();
                if (Math.ceil(num.doubleValue()) == num.longValue())
                    return num.longValue();
                return num.doubleValue();
            }
        }
        return null;
    }
}
