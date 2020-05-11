package io.frictionlessdata.tableschema.fk;

import java.util.*;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;

import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.util.JsonUtil;

public class ForeignKeyTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testValidStringFields() throws ForeignKeyException {
        Reference ref = new Reference("aResource", Arrays.asList("refField"), true);
        ForeignKey fk = new ForeignKey(Arrays.asList("fkField"), ref, true);

        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assert.assertNotNull(fk);
    }

    @Test
    public void testValidArrayFields() throws ForeignKeyException {
        String fkJson = "{\"fields\":[\"fkField1\", \"fkField2\"], \"reference\": {\"resource\": \"aResource\", \"fields\":[\"refField1\", \"refField2\"]}}";
        ForeignKey fk = ForeignKey.fromJson(fkJson, true);

        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assert.assertNotNull(fk);
    }

    @Test
    public void testNullFields() throws ForeignKeyException {
        Reference ref = new Reference("aResource", Arrays.asList("aField"), true);

        exception.expectMessage("A foreign key must have the fields and reference properties.");
        new ForeignKey(null, ref, true);
    }

    @Test
    public void testNullReference() throws ForeignKeyException{
        ForeignKey fk = new ForeignKey(true);
        fk.setFields(Arrays.asList("aField"));

        exception.expectMessage("A foreign key must have the fields and reference properties.");
        fk.validate();
    }

    @Test
    public void testNullFieldsAndReference() throws ForeignKeyException{
        ForeignKey fk = new ForeignKey(true);
        exception.expectMessage("A foreign key must have the fields and reference properties.");
        fk.validate();
    }

    @Test
    public void testFieldsNotStringOrArray() throws ForeignKeyException{
        exception.expectMessage("The foreign key's fields property must be a string or an array.");
        ForeignKey.fromJson("{\"fields\": 25}", true);
    }

    @Test
    public void testFkFieldsIsStringAndRefFieldsIsArray() throws ForeignKeyException{
        List<String> refFields = new ArrayList<>();
        refFields.add("field1");
        refFields.add("field2");
        refFields.add("field3");

        Reference ref = new Reference("aResource", refFields, true);
        
        Map<String, Object> fkMap = new HashMap<>();
        fkMap.put("fields", "aStringField");
        fkMap.put("reference", JsonUtil.getInstance().convertValue(ref, JsonNode.class));
        
        String fkJson = JsonUtil.getInstance().serialize(fkMap);

        exception.expectMessage("The reference's fields property must be a string if the outer fields is a string.");
        ForeignKey.fromJson(fkJson, true);
    }

    @Test
    public void testFkFieldsIsArrayAndRefFieldsIsString() throws ForeignKeyException{
        Reference ref = new Reference("aResource", Arrays.asList("aStringField"), true);

        List<String> fkFields = new ArrayList<>();
        fkFields.add("field1");
        fkFields.add("field2");
        fkFields.add("field3");
        
        Map<String, Object> fkMap = new HashMap<>();
        fkMap.put("fields", fkFields);
        fkMap.put("reference", JsonUtil.getInstance().convertValue(ref, JsonNode.class));
        
        String fkJson = JsonUtil.getInstance().serialize(fkMap);

        exception.expectMessage("The reference's fields property must be an array if the outer fields is an array.");
        ForeignKey.fromJson(fkJson, true);
    }

    @Test
    public void testFkAndRefFieldsDifferentSizeArray() throws ForeignKeyException{
    	List<String> refFields = new ArrayList<>();
        refFields.add("refField1");
        refFields.add("refField2");
        refFields.add("refField3");
        
        Map<String, Object> refMap = new HashMap<>();
        refMap.put("fields", refFields);
        refMap.put("resource", "aResource");

        List<String> fkFields = new ArrayList<>();
        fkFields.add("field1");
        fkFields.add("field2");
        
        Map<String, Object> fkMap = new HashMap<>();
        fkMap.put("fields", fkFields);
        fkMap.put("reference", refMap);
        
        String fkJson = JsonUtil.getInstance().serialize(fkMap);

        exception.expectMessage("The reference's fields property must be an array of the same length as that of the outer fields' array.");
        ForeignKey.fromJson(fkJson, true);
    }
}
