package io.frictionlessdata.tableschema.datasourceformat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.util.JsonUtil;

/**
 *
 */
public class JsonArrayDataSourceFormat extends AbstractDataSourceFormat {

    public JsonArrayDataSourceFormat(String json){
        super();
        this.dataSource = JsonUtil.getInstance().createArrayNode(DataSourceFormat.trimBOM(json));
    }

    public JsonArrayDataSourceFormat (InputStream inStream) throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader)) {
            String content = br.lines().collect(Collectors.joining("\n"));
            this.dataSource = JsonUtil.getInstance().createArrayNode(DataSourceFormat.trimBOM(content));
        }
    }

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is
     * null, then the file will be written as RFC 4180 compliant CSV
     * @param out the Writer to write to
     * @param format the CSV format to use
     * @param sortedHeaders the header row names in the order in which data should be
     *                      exported
     */
    //@Override
    /*public void writeCsv(Writer out, CSVFormat format, String[] sortedHeaders) {
        if (null == sortedHeaders) {
            throw new TableSchemaException("Json array-based tables must have externally set headers");
        }
        try {
            CSVFormat locFormat = (null != format)
                    ? format
                    : DataSourceFormat.getDefaultCsvFormat();

            locFormat = locFormat.withHeader(sortedHeaders);
            CSVPrinter csvPrinter = new CSVPrinter(out, locFormat);
            ArrayNode data = (ArrayNode)this.dataSource;
            for (Object record : data) {
                JsonNode obj = (JsonNode) record;
                writeDataRow(obj, sortedHeaders, csvPrinter);
            }
            csvPrinter.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void writeDataRow(JsonNode data, String[] sortedHeaders, CSVPrinter csvPrinter) {
        try {
            int recordLength = sortedHeaders.length;
            String[] sortedRec = new String[recordLength];
            for (int i = 0; i < recordLength; i++) {
                String key = sortedHeaders[i];
                if (data.has(key)) {
                	if (data.get(key).isTextual()) {
                		sortedRec[i] = data.get(key).asText();
                	} else {
                        sortedRec[i] = data.get(key).toString();
                    }
                }
            }
            csvPrinter.printRecord(sortedRec);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void write(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile))) {
            out.write(dataSource.toString());
        } catch (Exception e) {
            throw e;
        }
    }

     */

    @Override
    public boolean hasReliableHeaders() {
        return false;
    }

    /**
     * Retrieve the CSV Parser.
     *
     *
     * @return a CSVParser instance that works CSV data generated by converting the JSON-array data to CSV
     * @throws Exception thrown if the parser throws an exception
     */
    @Override
    CSVParser getCSVParser() throws Exception {
        String dataCsv = null;
        if(dataSource instanceof ArrayNode){

        	ByteArrayOutputStream output = new ByteArrayOutputStream();
        	
        	Builder csvSchemaBuilder = CsvSchema.builder();
        	JsonNode firstObject = ((ArrayNode)dataSource).elements().next();
        	Map<String, JsonNode> fields = new HashMap<>();
        	firstObject.fields().forEachRemaining(f->{
        		fields.put(f.getKey(), f.getValue());
        		csvSchemaBuilder.addColumn(f.getKey());
        	});
        	
        	try {
        		int i = 0;
        		for (String key : fields.keySet()) {
        			i++;
        			output.write(key.getBytes());
        			if (i < fields.size()) {
        				output.write(",".getBytes());
        			} else {
        				output.write("\n".getBytes());
        			}
				}
			} catch (IOException e) {
				throw new TableSchemaException(e);
			}
        	
        	CsvSchema csvSchema = csvSchemaBuilder.build();
        	CsvMapper csvMapper = getCsvMapper();
        	ObjectWriter objectWriter = csvMapper.writerFor(Map.class).with(csvSchema);
        	
        	((ArrayNode) dataSource).elements().forEachRemaining(n->{
        		Map<String, Object> row = new HashMap<>();
        		n.fields().forEachRemaining(f->{
        			row.put(f.getKey(), f.getValue());
        		});
        		try {
        			objectWriter.writeValue(output, row);
				} catch (IOException e) {
					throw new TableSchemaException(e);
				}
        	});

            dataCsv = output.toString();
        } else{
            throw new TableSchemaException("Data source is of invalid type.");
        }
        Reader sr = new StringReader(dataCsv);
        // Get the parser.
        return CSVParser.parse(sr, DataSourceFormat.getDefaultCsvFormat());
    }

	private CsvMapper getCsvMapper() {
		return CsvMapper.builder()
				.addModule(complexObjectSerializationModule())
				.build();
	}
	
	private Module complexObjectSerializationModule() {
		SimpleModule module = new SimpleModule();
		module.setSerializers(new SimpleSerializers());
		module.addSerializer(ObjectNode.class, mapSerializer());
		return module;
	}

	private JsonSerializer<ObjectNode> mapSerializer() {
		return new JsonSerializer<ObjectNode>() {

			@Override
			public void serialize(ObjectNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
				gen.writeString(value.toString());
			}
			
		};
	}
}
