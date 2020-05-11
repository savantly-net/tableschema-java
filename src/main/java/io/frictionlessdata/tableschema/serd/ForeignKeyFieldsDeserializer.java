package io.frictionlessdata.tableschema.serd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.frictionlessdata.tableschema.exception.ForeignKeyException;

public class ForeignKeyFieldsDeserializer extends StdDeserializer<List<String>> { 
 
    public ForeignKeyFieldsDeserializer() { 
        this(null); 
    } 
 
    public ForeignKeyFieldsDeserializer(Class<?> vc) { 
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
        			throw new ForeignKeyException("when using an array as the foreign key's fields property, the items must be strings. but found: " + node.toString());
        		}
        	});
        } else {
        	throw new ForeignKeyException("The foreign key's fields property must be a string or an array.");
        }
        return list;
    }
}