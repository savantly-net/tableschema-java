package io.frictionlessdata.tableschema.serd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.frictionlessdata.tableschema.exception.PrimaryKeyException;

public class SchemaPrimaryKeyDeserializer extends StdDeserializer<List<String>> { 
 
    public SchemaPrimaryKeyDeserializer() { 
        this(null); 
    } 
 
    public SchemaPrimaryKeyDeserializer(Class<?> vc) { 
        super(vc); 
    }
 
    @Override
    public List<String> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    	List<String> list = new ArrayList<>();
        JsonNode node = jp.getCodec().readTree(jp);
        if(node.isTextual()) {
        	list.add(node.asText());
        } else if(node.isArray()) {
        	node.forEach(f->{
        		if(f.isTextual()) {
        			list.add(f.asText());
        		} else {
        			throw new PrimaryKeyException("when using an array as the primaryKey property, the items must be strings. but found: " + node.toString());
        		}
        	});
        } else {
        	throw new PrimaryKeyException("The primaryKey property must be a string or an array.");
        }
        return list;
    }
}
