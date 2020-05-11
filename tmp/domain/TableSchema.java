import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Table Schema
 * <p>
 * A Table Schema for this resource, compliant with the [Table Schema](/tableschema/) specification.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "fields",
    "primaryKey",
    "foreignKeys",
    "missingValues"
})
public class TableSchema {

    /**
     * An `array` of Table Schema Field objects.
     * (Required)
     * 
     */
    @JsonProperty("fields")
    @JsonPropertyDescription("An `array` of Table Schema Field objects.")
    private List<Field> fields = new ArrayList<Field>();
    /**
     * A primary key is a field name or an array of field names, whose values `MUST` uniquely identify each row in the table.
     * 
     */
    @JsonProperty("primaryKey")
    @JsonPropertyDescription("A primary key is a field name or an array of field names, whose values `MUST` uniquely identify each row in the table.")
    private Object primaryKey;
    @JsonProperty("foreignKeys")
    private List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();
    /**
     * Values that when encountered in the source, should be considered as `null`, 'not present', or 'blank' values.
     * 
     */
    @JsonProperty("missingValues")
    @JsonPropertyDescription("Values that when encountered in the source, should be considered as `null`, 'not present', or 'blank' values.")
    private List<String> missingValues = new ArrayList<String>(Arrays.asList(""));

    /**
     * An `array` of Table Schema Field objects.
     * (Required)
     * 
     */
    @JsonProperty("fields")
    public List<Field> getFields() {
        return fields;
    }

    /**
     * An `array` of Table Schema Field objects.
     * (Required)
     * 
     */
    @JsonProperty("fields")
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    /**
     * A primary key is a field name or an array of field names, whose values `MUST` uniquely identify each row in the table.
     * 
     */
    @JsonProperty("primaryKey")
    public Object getPrimaryKey() {
        return primaryKey;
    }

    /**
     * A primary key is a field name or an array of field names, whose values `MUST` uniquely identify each row in the table.
     * 
     */
    @JsonProperty("primaryKey")
    public void setPrimaryKey(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    @JsonProperty("foreignKeys")
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    @JsonProperty("foreignKeys")
    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    /**
     * Values that when encountered in the source, should be considered as `null`, 'not present', or 'blank' values.
     * 
     */
    @JsonProperty("missingValues")
    public List<String> getMissingValues() {
        return missingValues;
    }

    /**
     * Values that when encountered in the source, should be considered as `null`, 'not present', or 'blank' values.
     * 
     */
    @JsonProperty("missingValues")
    public void setMissingValues(List<String> missingValues) {
        this.missingValues = missingValues;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TableSchema.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("fields");
        sb.append('=');
        sb.append(((this.fields == null)?"<null>":this.fields));
        sb.append(',');
        sb.append("primaryKey");
        sb.append('=');
        sb.append(((this.primaryKey == null)?"<null>":this.primaryKey));
        sb.append(',');
        sb.append("foreignKeys");
        sb.append('=');
        sb.append(((this.foreignKeys == null)?"<null>":this.foreignKeys));
        sb.append(',');
        sb.append("missingValues");
        sb.append('=');
        sb.append(((this.missingValues == null)?"<null>":this.missingValues));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.foreignKeys == null)? 0 :this.foreignKeys.hashCode()));
        result = ((result* 31)+((this.fields == null)? 0 :this.fields.hashCode()));
        result = ((result* 31)+((this.primaryKey == null)? 0 :this.primaryKey.hashCode()));
        result = ((result* 31)+((this.missingValues == null)? 0 :this.missingValues.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TableSchema) == false) {
            return false;
        }
        TableSchema rhs = ((TableSchema) other);
        return (((((this.foreignKeys == rhs.foreignKeys)||((this.foreignKeys!= null)&&this.foreignKeys.equals(rhs.foreignKeys)))&&((this.fields == rhs.fields)||((this.fields!= null)&&this.fields.equals(rhs.fields))))&&((this.primaryKey == rhs.primaryKey)||((this.primaryKey!= null)&&this.primaryKey.equals(rhs.primaryKey))))&&((this.missingValues == rhs.missingValues)||((this.missingValues!= null)&&this.missingValues.equals(rhs.missingValues))));
    }

}
