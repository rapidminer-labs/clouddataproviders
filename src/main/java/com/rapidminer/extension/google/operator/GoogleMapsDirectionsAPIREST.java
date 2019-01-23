package com.rapidminer.extension.google.operator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.error.AttributeNotFoundError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Scott Genzer
 */
public class GoogleMapsDirectionsAPIREST extends Operator {

    // Ports
    private InputPort inputPort = getInputPorts().createPort("exa", ExampleSet.class);
	private final OutputPort outputPort = getOutputPorts().createPort("out");
	private OutputPort oriPort = getOutputPorts().createPort("ori");

	public static final String PARAMETER_API_KEY = "API key";
	public static final String PARAMETER_ORIGIN_ADDRESS = "Origin address";
	public static final String PARAMETER_DESTINATION_ADDRESS = "Destination address";

	@Resource
	private WebServiceContext context;

	public GoogleMapsDirectionsAPIREST(OperatorDescription description) throws UndefinedParameterError {
		super(description);
		 getTransformer().addPassThroughRule(inputPort, oriPort);
	}

	@Override
	public void doWork() throws OperatorException {
		
		ExampleSet exampleSet = inputPort.getData(ExampleSet.class);
		Attributes attributes = exampleSet.getAttributes();
		oriPort.deliver(exampleSet);
		
		String key = getParameterAsString(PARAMETER_API_KEY);
        if (key == null) {
            throw new AttributeNotFoundError(this, PARAMETER_API_KEY, "");
        }
        Attribute keyAttribute = exampleSet.getAttributes().get(key);

        String originAddress = getParameterAsString(PARAMETER_ORIGIN_ADDRESS);
        if (key == null) {
            throw new AttributeNotFoundError(this, PARAMETER_ORIGIN_ADDRESS, "");
        }
        Attribute originAddressAttribute = exampleSet.getAttributes().get(originAddress);
		
        String destinationAddress = getParameterAsString(PARAMETER_DESTINATION_ADDRESS);
        if (key == null) {
            throw new AttributeNotFoundError(this, PARAMETER_DESTINATION_ADDRESS, "");
        }
        Attribute destinationAddressAttribute = exampleSet.getAttributes().get(destinationAddress);

		Attribute jsonOutputAttribute = AttributeFactory.createAttribute("JSON", Ontology.STRING);
		exampleSet.getExampleTable().addAttribute(jsonOutputAttribute);
		attributes.addRegular(jsonOutputAttribute);
		
		Attribute statusAttribute = AttributeFactory.createAttribute("Status", Ontology.STRING);
		exampleSet.getExampleTable().addAttribute(statusAttribute);
		attributes.addRegular(statusAttribute);

		String response = null;

		for(Example e: exampleSet){
			
			String a1 = e.getValueAsString(originAddressAttribute);
			String a2 = e.getValueAsString(destinationAddressAttribute);
			String status = "";

			try {
				response = this.queryOperation(key, a1, a2);				
				javax.json.JsonReader reader = javax.json.Json.createReader(new StringReader(response));
				javax.json.JsonObject object = reader.readObject();				
				reader.close();

				status = object.getString("status");
				

			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			e.setValue(statusAttribute, status);
			e.setValue(jsonOutputAttribute,response);
			
		}

		outputPort.deliver(exampleSet);
		
	}

	private String queryOperation(String key, String originAddress, String destinationAddress) throws IOException, InterruptedException {

		// encode addresses to UTF-8
		String urlEncodedOriginAddress = URLEncoder.encode(originAddress, "UTF-8");
		String urlEncodedDestinationAddress = URLEncoder.encode(destinationAddress, "UTF-8");

		// create URL
		String tempURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + urlEncodedOriginAddress + "&destination=" + urlEncodedDestinationAddress + "&key=" + key;

		URL url = new URL(tempURL);

		// make GET request
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();

		BufferedReader in = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		String output = response.toString();

		return output;

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
        types.add(
                new ParameterTypeString(PARAMETER_API_KEY,"API Key",false,false)
        );
        types.add(
                new ParameterTypeAttribute(PARAMETER_ORIGIN_ADDRESS,"Origin address",inputPort)
        );
        types.add(
                new ParameterTypeAttribute(PARAMETER_DESTINATION_ADDRESS,"Destination address",inputPort)
        );
        return types;
	}

}