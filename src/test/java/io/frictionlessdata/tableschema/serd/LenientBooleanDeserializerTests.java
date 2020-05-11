package io.frictionlessdata.tableschema.serd;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.frictionlessdata.tableschema.exception.JsonParsingException;
import io.frictionlessdata.tableschema.util.JsonUtil;

public class LenientBooleanDeserializerTests {


	@Test
	@DisplayName("Test truthy 'y'")
	public void yTest() {
		String yes = "\"y\"";
		Boolean result = deserialize(yes);
		assertTrue(result);
		
		result = deserialize(yes.toUpperCase());
		assertTrue(result);
	}
	
	@Test
	@DisplayName("Test falsey 'n'")
	public void nTest() {
		String no = "\"n\"";
		Boolean result = deserialize(no);
		assertFalse(result);
		
		result = deserialize(no.toUpperCase());
		assertFalse(result);
	}
	
	@Test
	public void nullValue() {
		// null values must deserialize as Boolean.FALSE to support primitives
		Boolean result = deserialize(null);		
		assertFalse(result);
	}
	
	@Test
	public void invalidValue() {
		Assertions.assertThrows(JsonParsingException.class, ()->{
			deserialize("\"never\"");
		});
	}

	private Boolean deserialize(String yesOrNo) {
		TestObject testObject = JsonUtil.getInstance().deserialize("{\"exists\":" + yesOrNo + "}", TestObject.class);
		
		return testObject.getExists();
	}
	
	private static class TestObject {
		
		@JsonDeserialize(using = LenientBooleanDeserializer.class)
		private final Boolean exists = null;
		
		public Boolean getExists() {
			return exists;
		}
	}
}
