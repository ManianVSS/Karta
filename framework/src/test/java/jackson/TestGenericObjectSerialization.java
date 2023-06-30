package jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

@Data
public class TestGenericObjectSerialization {
    public int data;
    public TestGenericObjectSerialization next;

    public static final TypeReference<HashMap<String, Serializable>> genericJacksonObjectType = new TypeReference<>() {
    };


    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.findAndRegisterModules();

//        Serializable myGenericObject = objectMapper.readValue(FileUtils.readFileToString(new File("MyGenericObject.json"), Charset.defaultCharset()), Serializable.class);

        TypeReference<HashMap<String, ArrayList<Serializable>>> stepDataTypeRef = new TypeReference<>() {
        };

        HashMap<String, ArrayList<Serializable>> stepData = objectMapper.readValue(FileUtils.readFileToString(new File("MyGenericObject.json"), Charset.defaultCharset()), stepDataTypeRef);
//        ArrayList<TestGenericObjectSerialization> testGenericObjectSerialization =
        System.out.println(stepData);
    }
}
