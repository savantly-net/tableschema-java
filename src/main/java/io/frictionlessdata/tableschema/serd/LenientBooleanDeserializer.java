package io.frictionlessdata.tableschema.serd;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LenientBooleanDeserializer extends JsonDeserializer<Boolean> {
	
	private List<String> trueValues = Arrays.asList("true", "yes", "y", "t", "1");
    private List<String> falseValues = Arrays.asList("false", "no", "n", "f", "0");

	@Override
	public Boolean deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = p.getCurrentToken();
		
		if (t.equals(JsonToken.VALUE_STRING)) {
			String text = p.getText().trim().toLowerCase();
			
			if (isTruthy(text)) {
				return Boolean.TRUE;
			} else if (isFalsey(text)) {
				return Boolean.FALSE;
			}
			
			throw ctxt.weirdStringException(text, Boolean.class, wierdStringExceptionMessage());
		} else if (t.equals(JsonToken.VALUE_NULL)) {
			return getNullValue();
		}
		
		// else throw an exception
        return (Boolean) ctxt.handleUnexpectedToken(Boolean.class, p);
	}
	
	private String wierdStringExceptionMessage() {
		return String.format("Only truthy values: %s, and falsey values: %s are supported", trueValues, falseValues);
	}

	private boolean isFalsey(String text) {
		return falseValues.contains(text);
	}

	private boolean isTruthy(String text) {
		return trueValues.contains(text);
	}

	@Override
	public Boolean getNullValue() {
		return Boolean.FALSE;
	}

}