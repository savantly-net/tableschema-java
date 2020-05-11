package io.frictionlessdata.tableschema.schema;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.frictionlessdata.tableschema.exception.*;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.fk.ForeignKey;
import io.frictionlessdata.tableschema.io.FileReference;
import io.frictionlessdata.tableschema.io.LocalFileReference;
import io.frictionlessdata.tableschema.io.URLFileReference;
import io.frictionlessdata.tableschema.serd.SchemaPrimaryKeyDeserializer;
import io.frictionlessdata.tableschema.util.JsonUtil;

/**
 *
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_EMPTY)
@JsonPropertyOrder({
    "fields",
    "primaryKey",
    "foreignKeys",
    "missingValues"
})
public class Schema {
    
    public static final String JSON_KEY_FIELDS = "fields";

	private JsonSchema tableJsonSchema = null;

    /**
     * An `array` of Table Schema Field objects.
     * (Required)
     *
     */
    @JsonProperty("fields")
    @JsonPropertyDescription("An `array` of Table Schema Field objects.")
    private List<Field> fields = new ArrayList();

    /**
     * A primary key is a field name or an array of field names, whose values `MUST` uniquely identify each row in the table.
     *
     */
    @JsonProperty("primaryKey")
    @JsonPropertyDescription("A primary key is a field name or an array of field names, whose values `MUST` uniquely identify each row in the table.")
    private List<String> primaryKey = new ArrayList();

    @JsonProperty("foreignKeys")
    private List<ForeignKey> foreignKeys = new ArrayList();

    /**
     * Values that when encountered in the source, should be considered as `null`, 'not present', or 'blank' values.
     *
     */
    @JsonProperty("missingValues")
    @JsonPropertyDescription("Values that when encountered in the source, should be considered as `null`, 'not present', or 'blank' values.")
    private List<String> missingValues = new ArrayList<String>(Arrays.asList(""));
    
    private boolean strictValidation = true;
    private List<Exception> errors = new ArrayList();

    @JsonIgnore
    FileReference reference;

    /**
     * Create an empty table schema without strict validation
     */
    public Schema(){
        this.initValidator();
    }

    /**
     * Create an empty table schema
     * @param strict whether to enforce strict validation
     */
    public Schema(boolean strict){
        this.strictValidation = strict;
        this.initValidator();
    }

    /**
     * Create and validate a new table schema using a collection of fields.
     *
     * @param fields the fields to use for the Table
     * @param strict whether to enforce strict validation
     * @throws ValidationException thrown if parsing throws an exception
     */
    public Schema(Collection<Field> fields, boolean strict) throws ValidationException{
        this.strictValidation = strict;
        this.fields = new ArrayList<>(fields);

        initValidator();
        validate();
    }

    /**
     * Read, create, and validate a table schema from an {@link java.io.InputStream}.
     *
     * @param inStream the InputStream to read the schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson (InputStream inStream, boolean strict) throws IOException {
        Schema schema = JsonUtil.getInstance().deserialize(inStream, Schema.class);
        schema.strictValidation = strict;
        schema.validate();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a remote location.
     *
     * @param schemaUrl the URL to read the schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(URL schemaUrl, boolean strict) throws Exception{
        FileReference reference = new URLFileReference(schemaUrl);
        Schema schema = fromJson (reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a FileReference.
     *
     * @param reference the File or URL to read schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson (FileReference reference, boolean strict) throws Exception {
        Schema schema = fromJson (reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a local {@link java.io.File}.
     *
     * @param schemaFile the File to read schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson (File schemaFile, boolean strict) throws Exception {
        FileReference reference = new LocalFileReference(schemaFile);
        Schema schema = fromJson (reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a JSON string.
     *
     * @param schemaJson the File to read schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public  static Schema fromJson (String schemaJson, boolean strict) throws IOException {
        return fromJson (new ByteArrayInputStream(schemaJson.getBytes()), strict);
    }
    
    /**
     * Infer the data types and return the generated schema.
     * @param data
     * @param headers
     * @return Schema generated from the inferred input
     * @throws TypeInferringException 
     */
    public static Schema infer(List<Object[]> data, String[] headers) throws TypeInferringException, IOException {
        return fromJson(TypeInferrer.getInstance().infer(data, headers), true);
    }

    /**
     * Infer the data types and return the generated schema.
     * @param data
     * @param headers
     * @param rowLimit
     * @return Schema generated from the inferred input
     * @throws TypeInferringException
     */
    public static Schema infer(List<Object[]> data, String[] headers, int rowLimit) throws TypeInferringException, IOException {
        return fromJson(TypeInferrer.getInstance().infer(data, headers, rowLimit), true);
    }
    
    
    /**
     * Initializes the schema from given stream.
     * Used for Schema class instantiation with remote or local schema file.
     * @param inStream the `InputStream` to read and parse the Schema from
     * @throws Exception when reading fails
     */
    /*
    private void initSchemaFromStream(InputStream inStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String schemaString = br.lines().collect(Collectors.joining("\n"));
        inputStreamReader.close();
        br.close();
        
        this.initFromSchemaJson(schemaString);
    } */

	/*
    private void initFromSchemaJson(String json) throws PrimaryKeyException, ForeignKeyException{
    	
        JsonNode schemaObj = JsonUtil.getInstance().readValue(json);
        // Set Fields
        if(schemaObj.has(JSON_KEY_FIELDS)){
        	TypeReference<List<Field>> fieldsTypeRef = new TypeReference<List<Field>>() {};
        	String fieldsJson = schemaObj.withArray(JSON_KEY_FIELDS).toString();
            this.fields.addAll(JsonUtil.getInstance().deserialize(fieldsJson, fieldsTypeRef));
        }
        
        // Set Primary Key
        if(schemaObj.has(JSON_KEY_PRIMARY_KEY)){
        	if(schemaObj.get(JSON_KEY_PRIMARY_KEY).isArray()) {
        		this.setPrimaryKey(schemaObj.withArray(JSON_KEY_PRIMARY_KEY));
        	} else {
        		this.setPrimaryKey(schemaObj.get(JSON_KEY_PRIMARY_KEY).asText());
        	}
        }
        
        // Set Foreign Keys
        if(schemaObj.has(JSON_KEY_FOREIGN_KEYS)){
            JsonNode fkJsonArray = schemaObj.withArray(JSON_KEY_FOREIGN_KEYS);
            fkJsonArray.forEach((f)->{
                ForeignKey fk = new ForeignKey(f.toString(), this.strictValidation);
                this.addForeignKey(fk);
                
                if(!this.strictValidation){
                    this.getErrors().addAll(fk.getErrors());
                } 
            });
        }
    }
        */
    
    private void initValidator(){
        // Init for validation
        InputStream tableSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/table-schema.json");
        this.tableJsonSchema = JsonSchema.fromJson(tableSchemaInputStream, strictValidation);
    }
    
    /**
     * Check if schema is valid or not.
     * @return
     */
    @JsonIgnore
    public boolean isValid(){
        try{
            validate();
            return ((null == errors) || (errors.isEmpty()));
        }catch(ValidationException ve){
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
	private void validate(String foundFieldName) throws ValidationException{
        Field foundField = fields
                .stream()
                .filter((f) -> f.getName().equals(foundFieldName))
                .findFirst()
                .orElse(null);
        if (null == foundField) {
            throw new ValidationException (String.format("%s: Primary key field %s not found", tableJsonSchema, foundFieldName));
        }
    }

    /**
     * Validate the loaded Schema. First do a formal validation via JSON schema,
     * then check foreign keys match to existing fields.
     *
     * Validation is strict or unstrict depending on how the package was
     * instantiated with the strict flag.
     * @throws ValidationException 
     */
    @JsonIgnore
    private void validate() throws ValidationException{
        try{
        	String json = this.getJson();
             if (Objects.nonNull(foreignKeys)) {
                 for (ForeignKey fk : foreignKeys) {
                	 fk.validate(true);
                     fk.getFields().forEach(f->{
                    	 validate(f);
                     });
                 }
             }
             this.tableJsonSchema.validate(json);
        }catch(ValidationException ve){
            if(this.strictValidation){
                throw ve;
            }else{
                this.getErrors().add(ve);
            }
        }
    }
    
    public List<Exception> getErrors(){
        return this.errors;
    }
    
    @JsonIgnore
    public String getJson(){
    	return JsonUtil.getInstance().serialize(this);
    }

    public Object[] castRow(String[] row) throws InvalidCastException{
        
        if(row.length != this.fields.size()){
            throw new InvalidCastException("Row length is not equal to the number of defined fields.");
        }
        
        try{
            Object[] castRow = new Object[this.fields.size()];
        
            for(int i=0; i<row.length; i++){
                Field field = this.fields.get(i);
                castRow[i] = field.parseValue(row[i], field.getFormat(), null);
            }

            return castRow;
            
        }catch(Exception e){
            throw new InvalidCastException(e);
        }
        
    }
    
    public void writeJson (File outputFile) throws IOException{
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            writeJson(fos);
        }
    }

    public void writeJson (OutputStream output) throws IOException{
        try (BufferedWriter file = new BufferedWriter(new OutputStreamWriter(output))) {
            file.write(this.getJson());
        }
    }
    
    public void addField(Field field){
        this.fields.add(field);
        this.validate();
    }

    /**
     * Add a field from a JSON string representation.
     * @param json serialized JSON oject
     */
    public void addField(String json){
        Field field = Field.fromJson(json);
        this.addField(field);
    }
    
    public List<Field> getFields(){
        return this.fields;
    }
    
    public Field getField(String name){
        Iterator<Field> iter = this.fields.iterator();
        while(iter.hasNext()){
            Field field = iter.next();
            if(field.getName().equalsIgnoreCase(name)){
                return field;
            }
        }
        return null;
    }
    
    public List<String> getFieldNames(){
        return fields
                .stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }
    
    public boolean hasField(String name){
        Field field = fields
            .stream()
            .filter((f) -> f.getName().equals(name))
            .findFirst()
            .orElse(null);
        return (null != field);
    }
    
    public boolean hasFields(){
        return !this.getFields().isEmpty();
    }


    public FileReference getReference() {
        return reference;
    }
    /**
     * Set single primary key with the option of validation.
     * @param key
     * @throws PrimaryKeyException 
     */
    /*
    @JsonFormat(with = {Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
    public void setPrimaryKey(String key) throws PrimaryKeyException{
        checkKey(key);
        this.primaryKey = Arrays.asList(key); 
    } */

    private void checkKey(String key) {
        if(!this.hasField(key)){
            PrimaryKeyException pke = new PrimaryKeyException("No such field as: " + key + ".");
            if(this.strictValidation){
                throw pke;
            }else{
                this.getErrors().add(pke);
            }
        }
    }

    @JsonDeserialize(using = SchemaPrimaryKeyDeserializer.class)
    public void setPrimaryKey(List<String> keys) throws PrimaryKeyException{
    	this.primaryKey.clear();
    	for (String key : keys) {
    		checkKey(key);
			this.primaryKey.add(key);
		}
    }

	public List<String> getPrimaryKey(){
    	return this.primaryKey;
    }

    public List<ForeignKey> getForeignKeys(){
        return this.foreignKeys;
    }
    
    public void addForeignKey(ForeignKey foreignKey){
        this.foreignKeys.add(foreignKey);
    }

    /**
     * Similar to {@link #equals(Object)}, but disregards the `format` property
     * to allow for Schemas that are similar except that Fields have no
     * defined format. Also treats null and empty string the same for `name` and
     * `type`.
     *
     * @param other the Field to compare against
     * @return true if the other Field is equals ignoring the format, false otherwise
     */
    public boolean similar(Schema other) {
        if (this == other) return true;
        boolean same = true;
        for (Field f : fields) {
            Field otherField = other.getField(f.getName());
            same = same & f.similar(otherField);
        }
        if (!same)
            return false;
        return Objects.equals(primaryKey, other.primaryKey) &&
                Objects.equals(foreignKeys, other.foreignKeys);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schema schema = (Schema) o;
        return Objects.equals(fields, schema.fields) &&
                Objects.equals(primaryKey, schema.primaryKey) &&
                Objects.equals(foreignKeys, schema.foreignKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, primaryKey, foreignKeys);
    }
}