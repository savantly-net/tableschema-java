package io.frictionlessdata.tableschema.fk;

import java.net.URL;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.serd.ReferenceFieldsDeserializer;
import io.frictionlessdata.tableschema.util.JsonUtil;

/**
 *
 * 
 */
public class Reference {
    
    private URL datapackage = null;
    private String resource = null;
    @JsonDeserialize(using = ReferenceFieldsDeserializer.class)
    private List<String> fields = null;
    
    public Reference(){
    }
    
    public Reference(String resource, List<String> fields) throws ForeignKeyException{
        this(resource, fields, false);
    }
    
    public Reference(String resource, List<String> fields, boolean strict) throws ForeignKeyException{
        this.resource = resource;
        this.fields = fields;
        
        if(strict){
            this.validate();
        }
    }
    
    public Reference(URL datapackage, String resource, List<String> fields) throws ForeignKeyException{
        this(datapackage, resource, fields, false);
    }
    
    public Reference(URL datapackage, String resource, List<String> fields, boolean strict) throws ForeignKeyException{
        this.resource = resource;
        this.fields = fields;
        this.datapackage = datapackage;
        
        if(strict){
            this.validate();
        }
    }
    
    public static Reference fromJson(String json, boolean strict) throws ForeignKeyException{
        Reference ref = JsonUtil.getInstance().deserialize(json, Reference.class);
        if(strict){
            ref.validate();
        }
        return ref;
    }
    
    public URL getDatapackage(){
        return this.datapackage;
    }
    
    public String getResource(){
        return this.resource;
    }
    
    public void setResource(String resource){
        this.resource = resource;
    }
    
    public List<String> getFields(){
        return this.fields;
    }
    
    public void setFields(List<String> fields){
        this.fields = fields;
    }
    
    public final void validate() throws ForeignKeyException{
        if(Objects.isNull(resource) || resource.isEmpty() || Objects.isNull(fields) || fields.isEmpty()){
            throw new ForeignKeyException("A foreign key's reference must have the fields and resource properties.");
        }
    }
    
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }
}
