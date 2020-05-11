package io.frictionlessdata.tableschema.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.frictionlessdata.tableschema.exception.JsonParsingException;
import io.frictionlessdata.tableschema.exception.JsonSerializingException;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.serd.LenientBooleanDeserializer;

public final class JsonUtil {

	private static JsonUtil instance;
	private ObjectMapper mapper;
	
	private JsonUtil() {
		this.mapper = JsonMapper.builder()
			.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
			.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.disable(DeserializationFeature.WRAP_EXCEPTIONS)
			.addModule(customDeserializers())
			.build();
	}
	
	private Module customDeserializers() {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Boolean.class, new LenientBooleanDeserializer());
		return module;
	}

	public static JsonUtil getInstance() {
		if (Objects.isNull(instance)) {
			instance = new JsonUtil();
		}
		return instance;
	}
	
	public ObjectNode createNode() {
		return mapper.createObjectNode();
	}
	
	public TextNode createTextNode(String value) {
		return new TextNode(value);
	}
	
	public JsonNode createNode(String content) {
		try {
			return mapper.readTree(content);
		} catch (JsonProcessingException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonParsingException(t);
			});
		}
	}
	
	public JsonNode createNode(Object content) {
		try {
			String json = mapper.writeValueAsString(content);
			try {
				return mapper.readTree(json);
			} catch (JsonMappingException e) {
				throw customExceptionOrDefault(e, (t)-> { 
					return new JsonParsingException(t);
				});
			} 
		} catch (JsonProcessingException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonSerializingException(t);
			});
		}
	}
	
	public ArrayNode createArrayNode() {
		return mapper.createArrayNode();
	}
	
	public ArrayNode createArrayNode(String content) {
		try {
			return (ArrayNode)mapper.readTree(content);
		} catch (JsonProcessingException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonParsingException(t);
			});
		}
	}
	
	public ArrayNode createArrayNode(Object content) {
		return (ArrayNode) createNode(content);
	}
	
	public String serialize(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonSerializingException(t);
			});
		}
	}
	
	public <T> T deserialize(String value, Class<T> clazz) {
		try {
			return mapper.readValue(sanitize(value), clazz);
		} catch (JsonProcessingException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonParsingException(t);
			});
		}
	}

	public Schema deserialize(InputStream value, Class<Schema> clazz) {
		try {
			return mapper.readValue(value, clazz);
		} catch (IOException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonParsingException(t);
			});
		}
	}
	
	public <T> T deserialize(String value, TypeReference<T> typeRef) {
		try {
			return mapper.readValue(sanitize(value), typeRef);
		} catch (JsonProcessingException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonParsingException(t);
			});
		}
	}
	
	public JsonNode readValue(String value) {
		try {
			return mapper.readTree(sanitize(value));
		} catch (JsonProcessingException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonParsingException(t);
			});
		}
	}
	
	public JsonNode readValue(InputStream value) {
		try {
			return mapper.readTree(value);
		} catch (IOException e) {
			throw customExceptionOrDefault(e, (t)-> { 
				return new JsonParsingException(t);
			});
		}
	}
	
	public <T> T convertValue(Object value, Class<T> clazz) {
		return mapper.convertValue(value, clazz);
	}
	
	// if it uses the extended double quote character sometimes found in CSV files
	private String sanitize(String string) {
		if(string.startsWith("[“") || string.startsWith("{“")) {
    		// replace both left and right versions
    		return string.replace("“", "\"").replace("”", "\"");
    	} else return string;
	}
	
	private RuntimeException customExceptionOrDefault(Exception ex, Function<Exception, RuntimeException> defaultExceptionWrapper) {
		if ( Objects.nonNull(ex) && TableSchemaException.class.isAssignableFrom(ex.getClass())){
			return (TableSchemaException)ex;
		} else if (Objects.nonNull(ex.getCause()) && TableSchemaException.class.isAssignableFrom(ex.getCause().getClass())) {
			return (TableSchemaException)ex.getCause();
		} else {
			return defaultExceptionWrapper.apply(ex);
		}	
	}
}
