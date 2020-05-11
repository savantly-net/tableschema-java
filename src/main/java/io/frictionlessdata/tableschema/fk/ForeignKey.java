package io.frictionlessdata.tableschema.fk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.serd.ForeignKeyFieldsDeserializer;
import io.frictionlessdata.tableschema.util.JsonUtil;

/**
 * 
 */
public class ForeignKey {
    private static final String JSON_KEY_FIELDS = "fields";
    private static final String JSON_KEY_REFERENCE = "reference";
    
    @JsonDeserialize(using = ForeignKeyFieldsDeserializer.class)
    private List<String> fields = new ArrayList<>();
    @JsonProperty(required = true)
    private Reference reference = null;
    
    private boolean strictValidation = false;
    private List<Exception> errors = new ArrayList();
    
    public ForeignKey(){   
    }
    
    public ForeignKey(boolean strict){  
        this();
        this.strictValidation = strict;
    }
    
    
    public ForeignKey(List<String> fields, Reference reference, boolean strict) throws ForeignKeyException{
        this.fields = fields;
        this.reference = reference;
        this.strictValidation = strict;
        this.validate();
    }

	public static ForeignKey fromJson(String json, boolean strict) throws ForeignKeyException{
    	ForeignKey fk = JsonUtil.getInstance().deserialize(json, ForeignKey.class);
        fk.strictValidation = strict;
        fk.validate();
        return fk;
    }
    
    public void setFields(List<String> fields){
        this.fields = fields;
    }
    
    public List<String> getFields(){
        return this.fields;
    }
    
    public void setReference(Reference reference){
        this.reference = reference;
    }
    
    public Reference getReference(){
        return this.reference;
    }
    
    public final void validate() throws ForeignKeyException {
    	validate(this.strictValidation);
    }
    
    public final void validate(boolean strict) throws ForeignKeyException {
        ForeignKeyException fke = null;
        
        if(Objects.isNull(fields) || fields.isEmpty() || Objects.isNull(reference)){
            fke = new ForeignKeyException("A foreign key must have the fields and reference properties.");
            
        }else {
        	if(this.fields.size() > 1 && !(this.reference.getFields().size() > 1)) {
        		fke = new ForeignKeyException("The reference's fields property must be an array if the outer fields is an array.");
        	} else if(this.fields.size() == 1 && this.reference.getFields().size() > 1) {
        		fke = new ForeignKeyException("The reference's fields property must be a string if the outer fields is a string.");
        	} else if(this.fields.size() != this.reference.getFields().size()) {
        		fke = new ForeignKeyException("The reference's fields property must be an array of the same length as that of the outer fields' array.");
        	}
        }

        if(fke != null){
            if(strict){
                throw fke;  
            }else{
                this.getErrors().add(fke);
            }           
        }

    }
    
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }
    
    public List<Exception> getErrors(){
        return this.errors;
    }
    
}
