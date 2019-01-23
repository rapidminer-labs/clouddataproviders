package  com.rapidminer.extension.google.operator;

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
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.ParameterTypeConfigurable;

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
public class GoogleMapsGeocodingAPIREST extends Operator {

    // Ports
    private InputPort inputPort = getInputPorts().createPort("exa", ExampleSet.class);
	private final OutputPort outputPort = getOutputPorts().createPort("out");
	private OutputPort oriPort = getOutputPorts().createPort("ori");

	// Google Cloud API key
	//public static final String PARAMETER_API_KEY = "API key";
	public static final String PARAMETER_CONFIG = "Select Config";
	// Address to be Geocoded
	public static final String PARAMETER_ADDRESS = "address";

	@Resource
	private WebServiceContext context;

	public GoogleMapsGeocodingAPIREST(OperatorDescription description) throws UndefinedParameterError {
		super(description);
		 getTransformer().addPassThroughRule(inputPort, oriPort);
	}

	@Override
	public void doWork() throws OperatorException {
		
		ExampleSet exampleSet = inputPort.getData(ExampleSet.class);
		Attributes attributes = exampleSet.getAttributes();

		oriPort.deliver(exampleSet);

		Attribute jsonOutputAttribute = AttributeFactory.createAttribute("JSON", Ontology.STRING);
		exampleSet.getExampleTable().addAttribute(jsonOutputAttribute);
		attributes.addRegular(jsonOutputAttribute);
		
		Attribute statusAttribute = AttributeFactory.createAttribute("Status", Ontology.STRING);
		exampleSet.getExampleTable().addAttribute(statusAttribute);
		attributes.addRegular(statusAttribute);

		String key = "";
		try {
			key =  ConfigurationManager
					.getInstance()
					.lookup(GoogleConfigurator.TYPE_ID,
							getParameterAsString(PARAMETER_CONFIG),
							getProcess().getRepositoryAccessor())
					.getParameter(GoogleConfigurator.PARAMETER_APIKEY);
		
		
		String response = null;
				
		Attribute addressAttribute = exampleSet.getAttributes().get(PARAMETER_ADDRESS);
				
		for(Example e: exampleSet){
			
			String address = e.getValueAsString(addressAttribute);
			String status = "";	

			try {
				response = this.queryOperation(key, address);
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
		}
		catch(Exception e)
		{
			throw new OperatorException(e.getMessage());
		}

		outputPort.deliver(exampleSet);
		
	}

	private String queryOperation(String key, String address) throws IOException, InterruptedException {

		// encode address to UTF-8
		String urlEncodedAddress = URLEncoder.encode(address, "UTF-8");
		
		
		
	

		// create URL
		String tempURL = "https://maps.googleapis.com/maps/api/geocode/json?address=" + urlEncodedAddress + "&key=" + key;

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
		//ParameterTypeString key = new ParameterTypeString(GoogleConfigurator.PARAMETER_APIKEY, "API Key", false, false);
		
		ParameterType key = new ParameterTypeConfigurable(PARAMETER_CONFIG,
				"Choose a Google connection", "GOOGLE");
		key.setOptional(false);
	
	
		
		
		ParameterTypeAttribute address = new ParameterTypeAttribute(PARAMETER_ADDRESS,"address",inputPort);

		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(key);
		parameters.add(address);
		return parameters;
	}

}