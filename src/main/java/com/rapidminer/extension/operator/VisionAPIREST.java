package com.rapidminer.extension.operator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.protobuf.ByteString;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.Ontology;

/**
 * @author Daniel Grzech
 */
public class VisionAPIREST extends Operator {
	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = Logger.getLogger(VisionAPIREST.class.getName());

	/**
	 * API Key for Google Cloud Platform project.
	 */
	private static final String API_KEY = "API key";
	/**
	 * Type of request--local or remote--corresponding to the Google Cloud
	 * Speech service recognize or longrunningrecognize methods.
	 */
	private static final String REQUEST_TYPE = "request type";
	/**
	 * Path to file to be transcribed.
	 */
	private static final String PATH_TO_FILE = "document file";
	/**
	 * URI to Cloud Storage Bucket with the file file to be transcribed.
	 */
	private static final String REMOTE_URI = "Cloud Storage Bucket URI";

	private final OutputPort outputExampleSetPort = getOutputPorts().createPort("example set");

	@Resource
	private WebServiceContext context;

	public VisionAPIREST(OperatorDescription description) throws UndefinedParameterError {
		super(description);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		ParameterTypeString key = new ParameterTypeString(API_KEY, "Google Cloud Platform API key.", false, false);

		String[] types = { "local", "remote" };
		ParameterTypeCategory requestType = new ParameterTypeCategory(REQUEST_TYPE,
				"Whether to transcribe a local file or a file stored on GCS.", types, 0);
		requestType.setOptional(false);
		requestType.setExpert(false);

		String[] docFileExtensions = { "jpg", "jpeg", "png" };
		ParameterTypeFile docFile = new ParameterTypeFile(PATH_TO_FILE, "Document file to process.", true,
				docFileExtensions);
		docFile.setExpert(false);
		int[] index = { 0 };
		docFile.registerDependencyCondition(new EqualTypeCondition(this, requestType.getKey(), types, false, index));

		ParameterTypeString remoteURL = new ParameterTypeString(REMOTE_URI,
				"URI that points to a file with the document. Only Google Cloud Storage URIs are supported.", true,
				false);
		int[] activationIndex = { 1 };
		remoteURL.registerDependencyCondition(
				new EqualTypeCondition(this, requestType.getKey(), types, false, activationIndex));

		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(key);
		parameters.add(requestType);
		parameters.add(docFile);
		parameters.add(remoteURL);

		return parameters;
	}

	/**
	 * Sends a POST request to Google Vision API in order to call the
	 * annotate method.
	 *
	 * @param key
	 *            API key to use.
	 * @param requestBody
	 *            JSON with the body of the request to send.
	 */
	private void annotate(String key, JSONObject requestBody) throws Exception {
		String tempURL = "https://vision.googleapis.com/v1/images:annotate?key=" + key;

		URL url = new URL(tempURL);

		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", "application/json");

		OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream());
		wr.write(requestBody.toString());
		wr.flush();
		wr.close();
		httpURLConnection.connect();

		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
		String output = "";

		while ((output = br.readLine()) != null) {
			sb.append(output);
		}

		JSONObject response = new JSONObject(sb.toString());
		parseResult(response);

	}

	/**
	 * Parses the JSON with the response of the Google Vision annotate
	 * method to an ExampleSet delivered at the output port of the operator,
	 * containing the transcription.
	 *
	 * @param response
	 *            Response of the Google Cloud Speech service to parse.
	 */
	private void parseResult(JSONObject response) {
		JSONArray results = response.getJSONArray("responses");
		JSONArray textAnnotations = results.getJSONObject(0).getJSONArray("textAnnotations");
		String description = textAnnotations.getJSONObject(0).get("description").toString();

		Attribute transcriptStringAttribute = AttributeFactory.createAttribute("transcript", Ontology.STRING);

		ExampleSetBuilder exampleSetBuilder = ExampleSets.from(transcriptStringAttribute);

		double[] values = new double[3];
		values[0] = transcriptStringAttribute.getMapping().mapString(description);

		exampleSetBuilder.addDataRow(new DoubleArrayDataRow(values));
		ExampleSet exampleSet = exampleSetBuilder.build();

		outputExampleSetPort.deliver(exampleSet);
	}

	@Override
	public void doWork() throws OperatorException {
		JSONArray requestBody;
		JSONObject request;
		JSONObject image;
		JSONArray features = new JSONArray();
		features.put(0, new JSONObject().put("type", "DOCUMENT_TEXT_DETECTION"));
		
		String fileName;
		String remoteURL;
		String key = this.getParameterAsString("API key");
		
		if (this.getParameterAsString("request type").equals("local")) {
			try {
				fileName = this.getParameterAsString("document file");
				java.nio.file.Path path = Paths.get(fileName);
				byte[] data = Files.readAllBytes(path);
				byte[] dataBase64 = Base64.encodeBase64(data);
				ByteString audioBytes = ByteString.copyFrom(dataBase64);

				image = new JSONObject().put("content", audioBytes.toStringUtf8());

				request = new JSONObject().put("image", image).put("features", features);
				requestBody = new JSONArray().put(0, request);

				annotate(key, new JSONObject().put("requests", requestBody));

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			try {

				remoteURL = this.getParameterAsString("Cloud Storage Bucket URI");
				image = new JSONObject().put("source", remoteURL);
				
				request = new JSONObject().put("image", image).put("features", features);
				requestBody = new JSONArray().put(0, request);

				annotate(key, new JSONObject().put("requests", requestBody));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}